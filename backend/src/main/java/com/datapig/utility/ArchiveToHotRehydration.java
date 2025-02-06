package com.datapig.utility;

import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobItem;



@Service
public class ArchiveToHotRehydration {

    private BlobServiceClient blobServiceClient;

    public ArchiveToHotRehydration(String storageAccountUrl,String sasToken) {
            // Create a BlobServiceClient using the storage account URL and SAS token
            this.blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(storageAccountUrl)
                    .sasToken(sasToken)
                    .buildClient();
    }

    // Method to rehydrate a blob or folder to Hot tier
    public boolean rehydrateToHotTier(String containerName, String path) {
        try {
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


    public boolean checkRehydrationStatus(String containerName, String path) {
        try {
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

