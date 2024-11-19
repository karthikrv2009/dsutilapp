package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder;
import com.azure.storage.file.datalake.models.PathProperties;
import com.datapig.component.EncryptedPropertyReader;
import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.MetaDataPointer;
import com.datapig.utility.JDBCTemplateUtiltiy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;

@Service
public class SynapseLogParserService {

    @Autowired
    private EncryptedPropertyReader encryptedPropertyReader;

    @Autowired
    private JDBCTemplateUtiltiy jdbcTemplateUtiltiy;

    @Autowired
    private MetaDataPointerService metaDataPointerService;

    @Autowired
    private FolderSyncStatusService folderSyncStatusService;

    @Autowired
    private PolybaseService polybaseService;

    public void startParse(String folderName) {

        String fileSystemName = encryptedPropertyReader.getProperty("STORAGE_ACCOUNT");
        String targetFileName = encryptedPropertyReader.getProperty("TARGET_FILENAME");

        String storageAccountUrl = encryptedPropertyReader.getProperty("STRORAGE_ACCOUNT_URL");

        String saskey = encryptedPropertyReader.getProperty("Storage_SAS_TOKEN");

        // ADLS Gen2 endpoint with SAS token
        String endpointWithSAS = storageAccountUrl + "/" + fileSystemName + "/?" + saskey;

        // Create a DataLakeFileSystemClient using SAS token
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
                .endpoint(endpointWithSAS)
                .fileSystemName(fileSystemName)
                .buildClient();

        // Existing Folder but not staged in FolderSyncStatus
        TreeSet<MetaDataPointer> existingFoldersNotStaged = existingFolderNotStaged();

        for (MetaDataPointer metaDataPointer : existingFoldersNotStaged) {
            // Get a reference to the directory
            DataLakeDirectoryClient directoryClient = fileSystemClient.getDirectoryClient(folderName);
            System.out.println(directoryClient.getDirectoryPath());
            if (doesFileExist(directoryClient, targetFileName)) {
                List<String> tablesInDB=getTablesPerFolderInDB(metaDataPointer);
                Set<String> tableNamesInAdls = jdbcTemplateUtiltiy
                        .getTableInFolder(metaDataPointer.getFolderName(), fileSystemName);
                for (String tableName : tableNamesInAdls) {
                    if(!tablesInDB.contains(tableName)){
                        loadFolderSyncStatus(metaDataPointer, tableName);
                    }
                }
                updateChangeLogToReady(metaDataPointer); // Update the status after processing
            }
        }

        MetaDataPointer metaDataPointerInDB= metaDataPointerService.getMetaDataPointer(folderName);

        if(metaDataPointerInDB==null){
            DataLakeDirectoryClient directoryClient = fileSystemClient.getDirectoryClient(folderName);
            MetaDataPointer metaDataPointer = loadMetaDataPointer(directoryClient, folderName,storageAccountUrl,fileSystemName);
            if (doesFileExist(directoryClient, targetFileName)) {

                Set<String> tableNamesInAdls = jdbcTemplateUtiltiy
                        .getTableInFolder(metaDataPointer.getFolderName(), fileSystemName);
                for (String tableName : tableNamesInAdls) {
                     loadFolderSyncStatus(metaDataPointer, tableName);
                }
                updateChangeLogToReady(metaDataPointer); // Update the status after processing
            }
        }

        
        TreeSet<MetaDataPointer> metaDataPointers=existingFolderStagedNotComplete(); 
        
        for(MetaDataPointer metaDataPointer:metaDataPointers){
            polybaseService.startSyncInFolder(metaDataPointer);
        }
    }

    //Method to get Table per folder from FolderSyncStatus in DB
    private List<String> getTablesPerFolderInDB(MetaDataPointer metaDataPointer){
        List<String> tables=new ArrayList<String>();
        List<FolderSyncStatus> folderSyncStatuss=folderSyncStatusService.getFolderSyncStatusByfolder(metaDataPointer.getFolderName());
        for(FolderSyncStatus folderSyncStatus:folderSyncStatuss){
            tables.add(folderSyncStatus.getTableName());
        }
        return tables;
    }

    // Method to check if a file exists in a specified directory
    private boolean doesFileExist(DataLakeDirectoryClient directoryClient, String fileName) {
        try {
            boolean flag = false;
            System.out.println(directoryClient.getDirectoryUrl());
            DataLakeFileClient fileClient = directoryClient.getFileClient(fileName);
            System.out.println(fileClient);
            while (!flag) {
                Thread.sleep(10000);
                if (fileClient != null) {
                    System.out.println("file size" + fileClient.getProperties().getFileSize());
                    if ((fileClient.getProperties().getFileSize() > 2000)) {
                        flag = true;
                    }
                } else {
                    flag = false;
                }
            }
            return flag; // If properties are fetched, file exists
        } catch (Exception e) {
            // Handle cases where file doesn't exist or other exceptions occur
            return false;
        }

    }

    // Method to print the lease status and creation timestamp of a directory
    private MetaDataPointer loadMetaDataPointer(DataLakeDirectoryClient directoryClient, String directoryName,
            String storageAccountUrl, String fileSystemName) {
        MetaDataPointer metaDataPointer = null;
        try {
            PathProperties properties = directoryClient.getProperties();
            // String leaseStatus = properties.getLeaseStatus() != null ?
            // properties.getLeaseStatus().toString() : "Unknown";
            Short copyStatus = 0;
            OffsetDateTime creationTime = properties.getCreationTime();
            metaDataPointer = new MetaDataPointer();
            metaDataPointer.setFolderName(directoryName);
            metaDataPointer.setAdlscreationtimestamp(creationTime.toLocalDateTime());
            metaDataPointer.setStageTime(LocalDateTime.now());
            metaDataPointer.setStageStatus(copyStatus);
            metaDataPointer.setStorageAccount(fileSystemName);
            metaDataPointer.setEnvironment(storageAccountUrl);
            metaDataPointer = metaDataPointerService.save(metaDataPointer);
            System.out.println("  Creation Time: " + (creationTime != null ? creationTime : "Unknown"));
        } catch (Exception e) {
            System.out.println("  Failed to retrieve properties for directory: " + directoryClient.getDirectoryPath());
        }
        return metaDataPointer;
    }

    private FolderSyncStatus loadFolderSyncStatus(MetaDataPointer metaDataPointer, String tableName) {
        Short copyStatus = 0;
        FolderSyncStatus folderSyncStatus = new FolderSyncStatus();
        folderSyncStatus.setFolder(metaDataPointer.getFolderName());
        folderSyncStatus.setTableName(tableName);
        folderSyncStatus.setCopyStatus(copyStatus);
        folderSyncStatus = folderSyncStatusService.save(folderSyncStatus);
        return folderSyncStatus;
    }

    private void updateChangeLogToReady(MetaDataPointer metaDataPointer) {
        Short stageStatus = 1;
        metaDataPointer.setStageTime(LocalDateTime.now());
        metaDataPointer.setStageStatus(stageStatus);
        metaDataPointerService.save(metaDataPointer);
    }

    private TreeSet<MetaDataPointer> existingFolderNotStaged() {
        Short copyStatus = 0;
        TreeSet<MetaDataPointer> metaDataPointers = metaDataPointerService.getMetaDataPointerBystageStatus(copyStatus);
        return metaDataPointers;
    }

    private TreeSet<MetaDataPointer> existingFolderStagedNotComplete() {
        Short copyStatus = 1;
        TreeSet<MetaDataPointer> metaDataPointers = metaDataPointerService.getMetaDataPointerBystageStatus(copyStatus);
        return metaDataPointers;
    }

}
