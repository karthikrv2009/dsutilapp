package com.datapig.service;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ADLSFileDownloaderService {

    @Autowired
    private PropertiesService propertyService;

    public void startDownload() {
        // Retrieve properties from the PropertyService
        String storageAccountUrl = propertyService.getPropertyValue("STRORAGE_ACCOUNT_URL");
        String sasToken = propertyService.getPropertyValue("Storage_SAS_TOKEN");
        String containerName = propertyService.getPropertyValue("STORAGE_ACCOUNT");
        String blobName = propertyService.getPropertyValue("BLOB_NAME");
        String localFilePath = propertyService.getPropertyValue("LOCAL_CHANGE_LOG");

        downloadFile(storageAccountUrl, sasToken, containerName, blobName, localFilePath);
    }

    public void downloadFile(String storageAccountUrl, String sasToken, String containerName, String blobName, String localFilePath) {
        System.out.println(storageAccountUrl + "=====>" + sasToken);

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
        } catch (BlobStorageException e) {
            System.err.println("Error downloading file from Azure Blob Storage: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}

