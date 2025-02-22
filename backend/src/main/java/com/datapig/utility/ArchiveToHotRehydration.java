package com.datapig.utility;

import org.springframework.stereotype.Service;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobStorageException;
import com.datapig.entity.DatabaseConfig;
import com.azure.storage.blob.models.BlobItem;

@Service
public class ArchiveToHotRehydration {

    public boolean rehydrateToHotTier(String containerName, String path, DatabaseConfig config) {
        try {
            System.out.println("Rehydrating ===> " + path);
            // Create a BlobServiceClient using the storage account URL and SAS token
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(config.getAdlsStorageAccountEndpoint())
                    .sasToken(config.getAdlsStorageAccountSasKey())
                    .buildClient();

            // Get the BlobContainerClient
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // List blobs in the specified path and rehydrate them
            PagedIterable<BlobItem> blobs = containerClient.listBlobsByHierarchy(path);
            for (BlobItem blobItem : blobs) {
                // Check if the item is not a directory
                if (!blobItem.isPrefix() && blobItem.getName().startsWith(path)) {
                    // Send each blob individually to the child method for rehydration
                    rehydrateBlobToHotTier(containerName, blobItem.getName(), config);
                }
            }
            return true; // Successful rehydration request
        } catch (BlobStorageException e) {
            System.err.println("Error rehydrating blob/folder: " + e.getMessage());
            return false; // Return false in case of error
        }
    }

    // Method to rehydrate a specific blob to the Hot tier
    public boolean rehydrateBlobToHotTier(String containerName, String blobName, DatabaseConfig config) {
        boolean flag = false;
        System.out.println("Rehydrate blob ===> " + blobName);
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
                flag = true; // Successfully rehydrated
            } else {
                System.out.println("Blob " + blobName + " is not in Archive tier. No rehydration needed.");
                flag = true; // Blob is not in Archive tier
            }
        } catch (BlobStorageException e) {
            System.err.println("Error rehydrating blob: " + e.getMessage());
            return false; // Return false in case of error
        }
        return flag;
    }

    public boolean checkRehydrationStatus(String containerName, String path, DatabaseConfig config) {
        System.out.println("Checking Rehydration Status:"+path);
        boolean flag = false;
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(config.getAdlsStorageAccountEndpoint())
                    .sasToken(config.getAdlsStorageAccountSasKey())
                    .buildClient();

            // Get the BlobContainerClient
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // List blobs in the specified path
            PagedIterable<BlobItem> blobs = containerClient.listBlobsByHierarchy(path);
            for (BlobItem blobItem : blobs) {
                // Check if the item is not a directory
                
                System.out.println("Blob in Check==>"+blobItem.getName());
                if (!blobItem.isPrefix() && blobItem.getName().startsWith(path)) {
                    // Send each blob individually to the child method for status check
                    flag = checkRehydrationStatusForBlob(containerName, blobItem.getName(), config) || flag;
                }
            }
            return flag; // Returns true if any blob is rehydrated
        } catch (BlobStorageException e) {
            System.err.println("Error checking rehydration status: " + e.getMessage());
            return false;
        }
    }

    // Method to check the rehydration status for a specific blob
    public boolean checkRehydrationStatusForBlob(String containerName, String blobName, DatabaseConfig config) {
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
}
