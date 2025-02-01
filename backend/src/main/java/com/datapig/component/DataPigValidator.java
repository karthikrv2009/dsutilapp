package com.datapig.component;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import com.azure.storage.queue.models.QueueStorageException;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.stereotype.Component;



@Component
public class DataPigValidator {

    public boolean checkDBConnection(String jdbcURL, String userName, String password, String driverClassName) {
        boolean isConnected = false;  // Default to false (connection failed)
        Connection connection = null;

        try {
            // Dynamically load the JDBC driver class
            Class.forName(driverClassName);

            // Attempt to establish a connection to the database
            connection = DriverManager.getConnection(jdbcURL, userName, password);

            // If no exception is thrown, the connection is successful
            System.out.println("Database connection successful!");
            isConnected = true;  // Set to true if connection is successful
        } catch (ClassNotFoundException e) {
            // If the driver class is not found
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            // Catch any SQL exceptions (e.g., invalid credentials, unreachable database)
            System.err.println("Database connection failed: " + e.getMessage());
        } finally {
            // Close the connection if it was successfully created
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }

        return isConnected;  // Return the connection status (true for success, false for failure)
    }

        public boolean checkADLSStorageConnection(String adlsStorageAccountEndpointURL, 
                                              String adlsStorageAccount, 
                                              String adlsContainerName, 
                                              String sasKey) {
        boolean isConnected = false;  // Default to false (failure)
        
        try {
            // Create a BlobServiceClient using SAS token
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(adlsStorageAccountEndpointURL)
                    .sasToken(sasKey)  // Use SAS Key for authentication
                    .buildClient();
            
            // Get the BlobContainerClient for the specified container
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(adlsContainerName);
            
            // Check if container exists and is accessible
            if (containerClient.getBlobContainerName() != null) {
                System.out.println("Connection to ADLS Storage successful!");
                isConnected = true; // Set to true if successful
            }
        } catch (BlobStorageException e) {
            System.err.println("ADLS Storage connection failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during connection: " + e.getMessage());
        }
        
        return isConnected; // Return the success status
    }

    // Method to check if the model.json blob exists in the root of the container
    public boolean checkModelJsonExistence(String adlsStorageAccountEndpointURL, 
                                           String adlsContainerName, 
                                           String sasKey) {
        boolean isModelJsonPresent = false;  // Default to false (blob not found)
        
        try {
            // Create a BlobServiceClient using SAS token
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(adlsStorageAccountEndpointURL)
                    .sasToken(sasKey)  // Use SAS Key for authentication
                    .buildClient();
            
            // Get the BlobContainerClient for the specified container
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(adlsContainerName);
            
            // List blobs in the container and check for model.json
            for (BlobItem blobItem : containerClient.listBlobs()) {
                if (blobItem.getName().equals("model.json")) {
                    System.out.println("model.json blob is present in the root of the container.");
                    isModelJsonPresent = true; // Set to true if model.json is found
                    break;  // Exit loop once found
                }
            }
        } catch (BlobStorageException e) {
            System.err.println("Error while checking model.json existence: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during blob existence check: " + e.getMessage());
        }
        
        return isModelJsonPresent; // Return the success status
    }

    public boolean checkADLSQueueConnection(String queueName, String adlsQueueEndpointURL, String adlsQueueSAS) {
        boolean isConnected = false;  // Default to false (connection failed)

        try {
            // Create a QueueServiceClient using SAS token for authentication
            QueueServiceClient queueServiceClient = new QueueServiceClientBuilder()
                    .endpoint(adlsQueueEndpointURL)  // Use the provided queue endpoint
                    .sasToken(adlsQueueSAS)  // Use SAS token for authentication
                    .buildClient();

            // Get the QueueClient for the specified queue
            QueueClient queueClient = queueServiceClient.getQueueClient(queueName);

            // Try to fetch the queue's properties to verify connection
            queueClient.getProperties();  // If this succeeds, the queue exists and is accessible

            System.out.println("ADLS Queue connection successful!");
            isConnected = true;  // Set to true if successful

        } catch (QueueStorageException e) {
            System.err.println("ADLS Queue connection failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during connection: " + e.getMessage());
        }

        return isConnected;  // Return the connection status (true for success, false for failure)
    }

    public boolean checkFilePathExist(String filePath) {
        boolean isExists = false;  // Default to false (folder not found)
    
        try {
            // Extract the parent folder from the provided file path
            Path folderPath = Paths.get(filePath).getParent();
    
            // Check if the extracted folder exists and is a directory
            if (folderPath != null && Files.exists(folderPath) && Files.isDirectory(folderPath)) {
                System.out.println("The folder exists at: " + folderPath);
                isExists = true;  // Set to true if the folder exists
            } else {
                System.err.println("The folder does not exist or is not a directory: " + folderPath);
            }
        } catch (Exception e) {
            System.err.println("Error checking folder existence: " + e.getMessage());
        }
    
        return isExists;  // Return the folder existence status (true for exists, false for doesn't exist)
    }
    
}
