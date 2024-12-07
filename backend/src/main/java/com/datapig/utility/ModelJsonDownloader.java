package com.datapig.utility;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.datapig.component.EncryptedPropertyReader;
import com.datapig.service.SynapseLogParserService;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ModelJsonDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ModelJsonDownloader.class);

    @Autowired
    private EncryptedPropertyReader propertyReader;

    private String storageAccountUrl;
    private String sasToken;
    private String containerName;
    private String blobName;
    private String localFilePath;

    public boolean downloadFile() {
        boolean flag = false;
        storageAccountUrl = propertyReader.getProperty("STRORAGE_ACCOUNT_URL");
        sasToken = propertyReader.getProperty("Storage_SAS_TOKEN");
        containerName = propertyReader.getProperty("STORAGE_ACCOUNT");
        blobName = propertyReader.getProperty("BLOB_NAME");
        localFilePath = propertyReader.getProperty("LOCAL_MOLDEL_JSON");

        logger.info("{} =====> {}", storageAccountUrl, sasToken);
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
            logger.info("File already exists at {}. It will be replaced.", localPath);
        }

        try {
            // Download the blob to a local file
            blobClient.downloadToFile(localPath.toString(), true);
            logger.info("File downloaded to: {}", localPath);
            flag = true;
        } catch (BlobStorageException e) {
            logger.error("Error downloading file from Azure Blob Storage: {}", e.getMessage(), e);
        } catch (Exception e) { // Catch all other exceptions
            logger.error("An error occurred: {}", e.getMessage(), e);
        }

        return flag;
    }

}
