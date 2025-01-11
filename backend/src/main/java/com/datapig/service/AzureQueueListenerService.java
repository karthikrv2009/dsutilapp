package com.datapig.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.QueueMessageItem;
import com.datapig.entity.DatabaseConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;

@Service
public class AzureQueueListenerService {

    private static final Logger logger = LoggerFactory.getLogger(AzureQueueListenerService.class);

    @Autowired
    private SynapseLogParserService synapseLogParserService;

    @Autowired
    private DatabaseConfigService databaseConfigService;

    private volatile boolean running = false;

    private Thread listenerThread;

    public void startQueueListener(String dbIdentifier) {
        DatabaseConfig databaseConfig=databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
        String queueName = databaseConfig.getQueueName();
        String queueSasToken = databaseConfig.getQueueSasToken();
        String sasQueueUrl = databaseConfig.getQueueEndpoint();

        running = true;
        listenerThread = new Thread(() -> listen(queueName, queueSasToken, sasQueueUrl, databaseConfig));
        listenerThread.start();
        logger.info("Azure Queue Listener started.");
    }

    @PreDestroy
    public void stopQueueListener() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        logger.info("Azure Queue Listener stopped.");
    }

    private void listen(String queueName, String queueSasToken, String sasQueueUrl, 
            DatabaseConfig databaseConfig) {

        QueueClient queueClient = new QueueClientBuilder()
                .endpoint(sasQueueUrl)
                .queueName(queueName)
                .sasToken(queueSasToken)
                .buildClient();

        while (running) {
            QueueMessageItem message = queueClient.receiveMessage();
            if (message != null) {
                processMessage(message , databaseConfig);
                queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());
            }

            try {
                Thread.sleep(10000); // Sleep for 10 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processMessage(QueueMessageItem message, DatabaseConfig databaseConfig) {

        // Decode the binary message to a string
        message.getBody().toString();
        // Base64 decode the message
        byte[] messageBytes = Base64.getDecoder().decode(message.getBody().toBytes());
        String decodedMessage = new String(messageBytes, StandardCharsets.UTF_8);
        logger.info(decodedMessage);
        // Parse the JSON message
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(decodedMessage);
            String rootModelJsonPath = databaseConfig.getAdlsCdmFilePath();
            // Extract the blob URL
            String blobUrl = jsonNode.path("data").path("blobUrl").asText();
            if (!blobUrl.equalsIgnoreCase(rootModelJsonPath)) {
                String initialURL = databaseConfig.getAdlsStorageAccountEndpoint() + "/"
                        + databaseConfig.getAdlsContainerName() + "/";
                int startIndex = initialURL.length();
                int endIndex = ("/model.json").length();
                logger.info("Processing blob: ", blobUrl);
                String folderName = blobUrl.substring(startIndex, blobUrl.length() - endIndex);
                synapseLogParserService.startParse(folderName, databaseConfig);
                // Add your processing logic here
            } else {
                logger.info("Blob does not match expected pattern: ", blobUrl);
            }
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            logger.warn(e.getMessage());
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            logger.warn(e.getMessage());
        }
    }

}
