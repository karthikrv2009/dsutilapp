package com.datapig.utility;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.datapig.component.EncryptedPropertyReader;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ModelJsonDownloader {
   
    @Autowired
    private EncryptedPropertyReader propertyReader;

    private String storageAccountUrl;
    private String sasToken;
    private String containerName;
    private String blobName;
    private String localFilePath;
    
    public boolean downloadFile() {
        boolean flag=false;
        storageAccountUrl = propertyReader.getProperty("STRORAGE_ACCOUNT_URL");
        sasToken = propertyReader.getProperty("Storage_SAS_TOKEN");
        containerName = propertyReader.getProperty("STORAGE_ACCOUNT");
        blobName=propertyReader.getProperty("BLOB_NAME");
        localFilePath=propertyReader.getProperty("LOCAL_MOLDEL_JSON");

        System.out.println(storageAccountUrl+"=====>"+sasToken);
        // Create a BlobServiceClient using the storage account URL and SAS token
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(storageAccountUrl)
                .sasToken(sasToken)
                .buildClient();

        // Get the container client
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Get the blob client
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        Path localPath = Paths.get(localFilePath);

        // Check if the local file already exists
        if (Files.exists(localPath)) {
            System.out.println("File already exists at " + localPath + ". It will be replaced.");
        }

        try {
            // Download the blob to a local file
            blobClient.downloadToFile(localPath.toString(), true);
            System.out.println("File downloaded to: " + localPath);
            flag=true;
        } catch (BlobStorageException e) {
            System.err.println("Error downloading file from Azure Blob Storage: " + e.getMessage());
        } catch (Exception e) { // Catch all other exceptions
            System.err.println("An error occurred: " + e.getMessage());
        }

        return flag;
    }

}
