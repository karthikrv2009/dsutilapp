package com.datapig.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;



@Service
public class AzureQueueListenerService {

   @Autowired
    private PropertiesService propertyService;

    @Autowired
    private ADLSFileDownloaderService adslFileDownloaderService;

    @Autowired
    private SynapseLogParserService synapseLogParserService;

    private volatile boolean running = false;

    private static String DATA_SOURCE;

    
    private Thread listenerThread;


    @Scheduled(fixedRate = 300000) // Runs every 5 minutes (300,000 milliseconds)
    public void startAzureQueueListener() {
        // Your Azure Queue listening logic here
        startQueueListener();
    }


    // Preserved regex pattern to match model.json inside any subdirectory under blobs
    private static final Pattern BLOB_PATTERN = Pattern.compile(
    		DATA_SOURCE + "/blobs/[^/]+/model\\.json$"
    );
    @PostConstruct
    public void startQueueListener() {
        String queueName = propertyService.getPropertyValue("QUEUE_NAME");
        String queueSasToken = propertyService.getPropertyValue("Queue_SAS_TOKEN");
        String sasQueueUrl = propertyService.getPropertyValue("SAS_QUEUE_URL");
        String changeLog = propertyService.getPropertyValue("LOCAL_CHANGE_LOG");

        running = true;
        listenerThread = new Thread(() -> listen(queueName, queueSasToken, sasQueueUrl, changeLog));
        listenerThread.start();
        System.out.println("Azure Queue Listener started.");
    }

    @PreDestroy
    public void stopQueueListener() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        System.out.println("Azure Queue Listener stopped.");
    }

    private void listen(String queueName, String queueSasToken, String sasQueueUrl, String changeLog) {

        int i=0;
        if(i==0) {
            adslFileDownloaderService.startDownload();
            System.out.println(changeLog);
            synapseLogParserService.startParse(changeLog);
            i=i+1;
        }

        QueueClient queueClient = new QueueClientBuilder()
            .endpoint(sasQueueUrl)
            .queueName(queueName)
            .sasToken(queueSasToken)
            .buildClient();

        while (running) {
            QueueMessageItem message = queueClient.receiveMessage();
            processMessage(message, changeLog);
            queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());

            try {
                Thread.sleep(10000); // Sleep for 10 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processMessage(QueueMessageItem message, String changeLog) {
            
        // Decode the binary message to a string
        message.getBody().toString();
        // Base64 decode the message
        byte[] messageBytes = Base64.getDecoder().decode(message.getBody().toBytes());
        String decodedMessage = new String(messageBytes, StandardCharsets.UTF_8);
        System.out.println(decodedMessage);
        // Parse the JSON message
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
		try {
			jsonNode = objectMapper.readTree(decodedMessage);
	        // Extract the blob URL
	        String blobUrl = jsonNode.path("data").path("blobUrl").asText();
	        if (BLOB_PATTERN.matcher(blobUrl).matches()) {
	            System.out.println("Processing blob: " + blobUrl);
	            adslFileDownloaderService.startDownload();
	            synapseLogParserService.startParse(changeLog);
	            // Add your processing logic here
	        } else {
	            System.out.println("Blob does not match expected pattern: " + blobUrl);
	        }
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
}
