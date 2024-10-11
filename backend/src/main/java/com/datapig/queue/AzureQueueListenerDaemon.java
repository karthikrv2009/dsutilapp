package com.datapig.queue;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.QueueMessageItem;
import com.datapig.config.PropertiesFileReader;
import com.datapig.file.ADLSFileDownloader;
import com.datapig.polybase.SynapseLogParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public class AzureQueueListenerDaemon implements Daemon {

    private static String QUEUE_NAME;
    private static String Queue_SAS_TOKEN; 
    private static String SAS_QUEUE_URL;
    private static String DATA_SOURCE;
    private static String CHANGE_LOG; 

    // Preserved regex pattern to match model.json inside any subdirectory under blobs
    private static final Pattern BLOB_PATTERN = Pattern.compile(
    		DATA_SOURCE + "/blobs/[^/]+/model\\.json$"
    );
    
    private volatile boolean running = false;
    private Thread listenerThread;

    @Override
    public void init(DaemonContext context) throws DaemonInitException {
        // Initialization logic, if necessary

    }

    @Override
    public void start() {
        running = true;
        listenerThread = new Thread(this::listen);
        listenerThread.start();
        System.out.println("Azure Queue Listener started.");
    }

    @Override
    public void stop() {
        running = false;
        listenerThread.interrupt(); // Interrupt the thread to stop it gracefully
        System.out.println("Azure Queue Listener stopped.");
    }

    @Override
    public void destroy() {
        // Clean-up logic, if necessary
    }

    private void listen() {
    	int i=0;
        if(i==0) {
            ADLSFileDownloader.startDownload();
            System.out.println(CHANGE_LOG);
            SynapseLogParser.startParse(CHANGE_LOG);
            i=i+1;
        }
    	QueueClient queueClient = new QueueClientBuilder()
                .endpoint(SAS_QUEUE_URL)
                .queueName(QUEUE_NAME)
                .sasToken(Queue_SAS_TOKEN) // Use SAS token for authentication
                .buildClient();

        while (running) {
            	QueueMessageItem message = queueClient.receiveMessage();
                processMessage(message);
                queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());
                
            try {
                Thread.sleep(10000); // Sleep for 10 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                break; // Exit the loop if interrupted
            }
        }
    }

    private void processMessage(QueueMessageItem message) {
        
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
	            ADLSFileDownloader.startDownload();
	            SynapseLogParser.startParse(CHANGE_LOG);
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

    public static void main(String[] args) {
    	HashMap<String, String> configs=PropertiesFileReader.getConfigs();
    	QUEUE_NAME=configs.get("QUEUE_NAME");
    	Queue_SAS_TOKEN=configs.get("Queue_SAS_TOKEN");
    	SAS_QUEUE_URL=configs.get("SAS_QUEUE_URL");
    	DATA_SOURCE=configs.get("DATA_SOURCE");
    	CHANGE_LOG=configs.get("LOCAL_CHANGE_LOG");
        AzureQueueListenerDaemon daemon = new AzureQueueListenerDaemon();
        try {
            daemon.start(); // For testing purposes
            //Thread.sleep(60000); // Run for 30 seconds before stopping
            //daemon.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
