package com.datapig.utility;

import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobStorageException;
import com.datapig.entity.DatabaseConfig;
import com.azure.storage.blob.models.BlobItem;



@Service
public class ArchiveToHotRehydration {


    // Method to rehydrate a blob or folder to Hot tier
    public boolean rehydrateToHotTier(String containerName, String path,DatabaseConfig config) {
        try {
            
              // Create a BlobServiceClient using the storage account URL and SAS token
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
        .endpoint(config.getAdlsStorageAccountEndpoint())
        .sasToken(config.getAdlsStorageAccountSasKey())
        .buildClient();
            // Get the BlobContainerClient
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // List blobs and check for the ones that need to be rehydrated
            containerClient.listBlobsByHierarchy(path).forEach(blobItem -> {
                // Check if the blob is in Archive tier and needs rehydration
                if (blobItem.getProperties().getAccessTier() == AccessTier.ARCHIVE) {
                    BlobClient blobClient = containerClient.getBlobClient(blobItem.getName());
                    blobClient.setAccessTier(AccessTier.HOT);
                    System.out.println("Rehydrating blob: " + blobItem.getName() + " to Hot tier.");
                }
            });
            return true; // Successful rehydration request
        } catch (BlobStorageException e) {
            System.err.println("Error rehydrating blob/folder: " + e.getMessage());
            return false; // Return false in case of error
        }
    }

    // Method to check the rehydration status for a specific blob
    public boolean checkRehydrationStatusForBlob(String containerName, String blobName,DatabaseConfig config) {
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(config.getAdlsStorageAccountEndpoint())
            .sasToken(config.getAdlsStorageAccountSasKey())
            .buildClient();
            // Get the BlobContainerClient
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Get the blob client for the specific blob
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Check the access tier of the specific blob
            if (blobClient.getProperties().getAccessTier() == AccessTier.HOT) {
                System.out.println("Blob " + blobName + " is successfully rehydrated to Hot tier.");
                return true; // Rehydrated successfully
            } else {
                System.out.println("Blob " + blobName + " is not in Hot tier.");
                return false; // Blob is not yet rehydrated
            }
        } catch (BlobStorageException e) {
            System.err.println("Error checking rehydration status for blob: " + e.getMessage());
            return false; // Return false in case of error
        }
    }

    // Method to rehydrate a specific blob to the Hot tier
    public boolean rehydrateBlobToHotTier(String containerName, String blobName,DatabaseConfig config) {
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(config.getAdlsStorageAccountEndpoint())
            .sasToken(config.getAdlsStorageAccountSasKey())
            .buildClient();
            // Get the BlobContainerClient
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Get the blob client for the specific blob
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Check if the blob is in Archive tier and rehydrate it
            if (blobClient.getProperties().getAccessTier() == AccessTier.ARCHIVE) {
                blobClient.setAccessTier(AccessTier.HOT);
                System.out.println("Rehydrating blob: " + blobName + " to Hot tier.");
                return true; // Successfully rehydrated
            } else {
                System.out.println("Blob " + blobName + " is not in Archive tier. No rehydration needed.");
                return false; // Blob is not in Archive tier
            }
        } catch (BlobStorageException e) {
            System.err.println("Error rehydrating blob: " + e.getMessage());
            return false; // Return false in case of error
        }
    }

    // Method to check if any blob in the folder has been rehydrated to Hot tier
    public boolean checkRehydrationStatus(String containerName, String path,DatabaseConfig config) {
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(config.getAdlsStorageAccountEndpoint())
            .sasToken(config.getAdlsStorageAccountSasKey())
            .buildClient();
            // Get the BlobContainerClient
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Check each blob in the folder
            for (BlobItem blobItem : containerClient.listBlobsByHierarchy(path)) {
                // Check the access tier of the blob
                if (blobItem.getProperties().getAccessTier() == AccessTier.HOT) {
                    System.out.println("Blob " + blobItem.getName() + " is successfully rehydrated to Hot tier.");
                    return true; // Rehydrated successfully
                } else {
                    System.out.println("Blob " + blobItem.getName() + " is not in Hot tier.");
                }
            }
            return false; // Not yet rehydrated
        } catch (BlobStorageException e) {
            System.err.println("Error checking rehydration status: " + e.getMessage());
            return false;
        }
    }
}

