package com.datapig.file;


import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.datapig.config.PropertiesFileReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;


public class ADLSFileDownloader {
    
    private String storageAccountUrl;
    private String sasToken;
    private String containerName;
    private String blobName;
    private String localFilePath;
    
    
    public ADLSFileDownloader() {
    	HashMap<String,String> configs=PropertiesFileReader.getConfigs();
        storageAccountUrl = configs.get("STRORAGE_ACCOUNT_URL");
        sasToken = configs.get("Storage_SAS_TOKEN");
        containerName = configs.get("STORAGE_ACCOUNT");
        blobName=configs.get("BLOB_NAME");
        localFilePath=configs.get("LOCAL_CHANGE_LOG");
    }

    public static void startDownload() {
        // String storageAccountUrl, String sasToken, String containerName,String blobName,String localFilePath
        ADLSFileDownloader downloader = new ADLSFileDownloader();
        downloader.downloadFile();
    }

    
    public void downloadFile() {
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
        } catch (BlobStorageException e) {
            System.err.println("Error downloading file from Azure Blob Storage: " + e.getMessage());
        } catch (Exception e) { // Catch all other exceptions
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

}

