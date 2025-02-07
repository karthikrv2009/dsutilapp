package com.datapig.utility;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.datapig.entity.DatabaseConfig;
import com.datapig.service.DatabaseConfigService;

import org.springframework.stereotype.Service;

import java.io.File;
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
    DatabaseConfigService databaseConfigService;

    public boolean downloadFile(String dbIdentifier) {
        DatabaseConfig databaseConfig=databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
        boolean flag = false;
        String storageAccountUrl = databaseConfig.getAdlsStorageAccountEndpoint();
        String sasToken = databaseConfig.getAdlsStorageAccountSasKey();
        String containerName = databaseConfig.getAdlsContainerName();
        String blobName = databaseConfig.getAdlsCdmFileName();
        String localFilePath = databaseConfig.getLocalCdmFilePath();

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
        // Method to create a subdirectory 'cdc' and update the file path
        public static String createSubDirectoryAndUpdatePath(String filePath) {
            // Convert the input file path to a Path object
            java.nio.file.Path path = Paths.get(filePath);
    
            // Get the parent directory of the file
            java.nio.file.Path parentPath = path.getParent();
    
            // Create the 'cdc' subdirectory within the parent folder
            if (parentPath != null) {
                File cdcDir = new File(parentPath.toString(), "cdc");
                if (!cdcDir.exists()) {
                    boolean created = cdcDir.mkdirs(); // Create the 'cdc' directory if it doesn't exist
                    if (created) {
                        System.out.println("Created 'cdc' subdirectory: " + cdcDir.getPath());
                    } else {
                        System.out.println("'cdc' directory already exists.");
                    }
                }
    
                // Construct the new file path with 'cdc' as a subdirectory
                java.nio.file.Path newFilePath = parentPath.resolve("cdc").resolve(path.getFileName());
                return newFilePath.toString();
            }
            
            return null;
        }
    
    public boolean downloadCdcFile(String dbIdentifier) {
        DatabaseConfig databaseConfig=databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
        String cdCFilePath=createSubDirectoryAndUpdatePath(databaseConfig.getAdlsCdmFilePath());

        boolean flag = false;
        String storageAccountUrl = databaseConfig.getAdlsStorageAccountEndpoint();
        String sasToken = databaseConfig.getAdlsStorageAccountSasKey();
        String containerName = databaseConfig.getAdlsContainerName();
        String blobName = databaseConfig.getAdlsCdmFileName();
        String localFilePath = cdCFilePath;

        if(cdCFilePath!=null){
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
        }
        
        return flag;
    }

}
