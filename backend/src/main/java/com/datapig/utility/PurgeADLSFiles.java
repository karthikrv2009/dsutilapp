package com.datapig.utility;

import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobContainerClient;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.datapig.entity.DatabaseConfig;

@Service
public class PurgeADLSFiles {

    public boolean deleteAllFilesInFolder(DatabaseConfig config, String containerName,String folderPath) {
        boolean flag=false;
        System.out.println("Folder to be purged : " + folderPath);
        // Create a BlobContainerClient using the SAS URL
        // Create a BlobServiceClient using the storage account URL and SAS token
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(config.getAdlsStorageAccountEndpoint())
                .sasToken(config.getAdlsStorageAccountSasKey())
                .buildClient();

        // Get the BlobContainerClient
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // List blobs in the folder
        containerClient.listBlobsByHierarchy(folderPath).forEach(blobItem -> {
            // Delete each blob
            if (!blobItem.isPrefix()) {
                containerClient.getBlobClient(blobItem.getName()).delete();
                System.out.println("Deleted: " + blobItem.getName());
            }
        });
        flag=true;
        System.out.println("All files in folder " + folderPath + " have been deleted.");
        return flag;
    }

}
