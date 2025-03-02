package com.datapig.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.QueueMessageItem;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.IntialLoad;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import org.slf4j.Logger;

@Service
public class AzureQueueListenerService {

    private static final Logger logger = LoggerFactory.getLogger(AzureQueueListenerService.class);

    private static String queueDbIdentifier; // Db Identifier for the current profile

    @Autowired
    private SynapseLogParserService synapseLogParserService;

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Lazy
    @Autowired
    private InitialLoadService initialLoadService;

    @Autowired
    private PipelineService pipelineService; 

    private static boolean running = false;

    private ExecutorService executorService;  // ExecutorService to manage multiple listeners
    private List<Future<?>> listenerFutures;  // List to track all listener tasks

    // Checks pipeline status, returns true if no pipelines are running
    private boolean pipelineStatus(String dbIdentifier) {
        int count = pipelineService.countPipelineInProgress(dbIdentifier);
        return count == 0;
    }

    public void startQueueListener(String dbIdentifier) {
        DatabaseConfig databaseConfig = databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
        if (databaseConfig != null) {
            queueDbIdentifier = dbIdentifier;  // Store current db identifier
            IntialLoad initialLoad = initialLoadService.getIntialLoad(dbIdentifier);
            if (initialLoad != null) {
                if (initialLoad.getQueueListenerStatus() == 0) {
                    initialLoad.setQueueListenerStatus(1);
                    initialLoadService.save(initialLoad); // Mark listener as active
                }
            }

            if (executorService == null || executorService.isShutdown()) {
                executorService = Executors.newCachedThreadPool();  // Create new thread pool
                listenerFutures = new ArrayList<>();
            }

            // Submit the task to listen to the Azure queue in parallel
            listenerFutures.add(executorService.submit(() -> listen(databaseConfig)));
            logger.info("Azure Queue Listener started for DB: " + dbIdentifier);
        }
    }

public void stopQueueListener() {
    running = false;  // Flag to indicate the listener should stop

    // Wait for the pipeline to complete before stopping
    while (!pipelineStatus(queueDbIdentifier)) {
        try {
            Thread.sleep(10000); // Wait until the pipeline is finished
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();  // Restore interrupt flag
            break;
        }
    }

    // Stop listener threads gracefully
    if (executorService != null) {
        for (Future<?> future : listenerFutures) {
            try {
                if (!future.isDone() && !future.isCancelled()) {
                    future.get();  // Block until the task finishes
                }
            } catch (CancellationException e) {
                // Task was cancelled, log this scenario
                logger.warn("Listener task was cancelled.");
            } catch (Exception e) {
                logger.error("Error waiting for listener thread to finish: ", e);
            }
        }

        executorService.shutdown();  // Shutdown executor service gracefully
    }

    logger.info("Azure Queue Listener stopped.");
}

    private void listen(DatabaseConfig databaseConfig) {
        String queueName = databaseConfig.getQueueName();
        String queueSasToken = databaseConfig.getQueueSasToken();
        String sasQueueUrl = databaseConfig.getQueueEndpoint();

        QueueClient queueClient = new QueueClientBuilder()
                .endpoint(sasQueueUrl)
                .queueName(queueName)
                .sasToken(queueSasToken)
                .buildClient();

        running = true;
        while (running) {
            QueueMessageItem message = queueClient.receiveMessage();
            if (message != null) {
                processMessage(message, databaseConfig);
                queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());
            } else {
                logger.info("No Message in the Queue for DB: " + databaseConfig.getDbIdentifier());
            }

            try {
                Thread.sleep(10000); // Sleep for 10 seconds before checking the queue again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;  // Exit the loop and end the thread
            }
        }
    }

    private void processMessage(QueueMessageItem message, DatabaseConfig databaseConfig) {
        // Decode the binary message to a string
        String decodedMessage = new String(Base64.getDecoder().decode(message.getBody().toBytes()), StandardCharsets.UTF_8);
        logger.info(decodedMessage);

        // Parse the JSON message
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(decodedMessage);
            String rootModelJsonPath = databaseConfig.getAdlsCdmFilePath();

            // Extract the blob URL from the JSON
            String blobUrl = jsonNode.path("data").path("blobUrl").asText();
            if (!blobUrl.equalsIgnoreCase(rootModelJsonPath)) {
                String initialURL = databaseConfig.getAdlsStorageAccountEndpoint() + "/"
                        + databaseConfig.getAdlsContainerName() + "/";
                int startIndex = initialURL.length();
                int endIndex = blobUrl.length() - ("/model.json").length();
                logger.info("Processing blob: " + blobUrl);
                String folderName = blobUrl.substring(startIndex, endIndex);
                synapseLogParserService.startParse(folderName, databaseConfig);
            } else {
                logger.info("Blob does not match expected pattern: " + blobUrl);
            }
        } catch (JsonProcessingException e) {
            logger.warn("Error processing message: " + e.getMessage());
        }
    }
}
