package com.datapig.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.QueueMessageItem;
import com.datapig.component.EncryptedPropertyReader;
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
    private EncryptedPropertyReader propertyReader;

    @Autowired
    private SynapseLogParserService synapseLogParserService;

    private volatile boolean running = false;

    private Thread listenerThread;

    public void startQueueListener(String dbIdentifier) {
        String queueName = propertyReader.getProperty("QUEUE_NAME");
        String queueSasToken = propertyReader.getProperty("Queue_SAS_TOKEN");
        String sasQueueUrl = propertyReader.getProperty("SAS_QUEUE_URL");
        String changeLog = propertyReader.getProperty("LOCAL_CHANGE_LOG");

        running = true;
        listenerThread = new Thread(() -> listen(queueName, queueSasToken, sasQueueUrl, changeLog, dbIdentifier));
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

    private void listen(String queueName, String queueSasToken, String sasQueueUrl, String changeLog,
            String dbIdentifier) {

        QueueClient queueClient = new QueueClientBuilder()
                .endpoint(sasQueueUrl)
                .queueName(queueName)
                .sasToken(queueSasToken)
                .buildClient();

        while (running) {
            QueueMessageItem message = queueClient.receiveMessage();
            if (message != null) {
                processMessage(message, changeLog, dbIdentifier);
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

    private void processMessage(QueueMessageItem message, String changeLog, String dbIdentifier) {

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
            String rootModelJsonPath = propertyReader.getProperty("ROOT_MODEL_JSON_PATH");
            // Extract the blob URL
            String blobUrl = jsonNode.path("data").path("blobUrl").asText();
            if (!blobUrl.equalsIgnoreCase(rootModelJsonPath)) {
                String initialURL = propertyReader.getProperty("STRORAGE_ACCOUNT_URL") + "/"
                        + propertyReader.getProperty("STORAGE_ACCOUNT") + "/";
                int startIndex = initialURL.length();
                int endIndex = ("/model.json").length();
                logger.info("Processing blob: ", blobUrl);
                String folderName = blobUrl.substring(startIndex, blobUrl.length() - endIndex);
                synapseLogParserService.startParse(folderName, dbIdentifier);
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
