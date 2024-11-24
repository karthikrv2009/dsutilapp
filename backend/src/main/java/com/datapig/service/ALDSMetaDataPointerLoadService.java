package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathProperties;
import com.datapig.component.EncryptedPropertyReader;
import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.IntialLoad;
import com.datapig.entity.MetaDataPointer;
import com.datapig.utility.JDBCTemplateUtiltiy;
import com.datapig.utility.ModelJsonDownloader;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;



@Service
public class ALDSMetaDataPointerLoadService {
    
    private IntialLoad intialLoad;

    @Autowired
    private ParseModelJson parseModelJson;

    @Autowired
    private ModelJsonDownloader modelJsonDownloader;

    
    @Autowired
    private MetaDataCatlogService metaDataCatlogService;

    @Autowired
    private final EncryptedPropertyReader encryptedPropertyReader;

    @Autowired
    private MetaDataPointerService metaDataPointerService;

    @Autowired
    private PolybaseService polybaseService;

    @Autowired
    private FolderSyncStatusService folderSyncStatusService;
    
    @Autowired
    private JDBCTemplateUtiltiy jDBCTemplateUtiltiy;

    @Autowired
    public ALDSMetaDataPointerLoadService(EncryptedPropertyReader encryptedPropertyReader) {
        this.encryptedPropertyReader = encryptedPropertyReader;
    }

    @Autowired
    InitialLoadService initialLoadService;

    public void load() {


        // Define the filesystem name and directory to search
        String fileSystemName = encryptedPropertyReader.getProperty("STORAGE_ACCOUNT");
        String targetFileName = encryptedPropertyReader.getProperty("TARGET_FILENAME");

        String storageAccountUrl = encryptedPropertyReader.getProperty("STRORAGE_ACCOUNT_URL");

        String saskey = encryptedPropertyReader.getProperty("Storage_SAS_TOKEN");

        // ADLS Gen2 endpoint with SAS token
        String endpointWithSAS = storageAccountUrl + "/" + fileSystemName + "/?" + saskey;

        intialLoad=initialLoadService.getIntialLoad("DBSynctUtilInitalLoad");
    if(intialLoad.getStatus()==0){
            intialLoad.setStatus(1);
            intialLoad.setStarttimestamp(LocalDateTime.now());
            intialLoad.setStagestarttime(LocalDateTime.now());
            intialLoad=initialLoadService.save(intialLoad);
        // Create a DataLakeFileSystemClient using SAS token
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
                .endpoint(endpointWithSAS)
                .fileSystemName(fileSystemName)
                .buildClient();
    // List first-level directories and check for the target file
        for (PathItem pathItem : fileSystemClient.listPaths()) {
            Set<String> tableNamesInDB = metaDataCatlogService.getAllTableName();            
            if (pathItem.isDirectory()) {
                String directoryName = pathItem.getName();
                if (directoryName.startsWith("20")) {
                    System.out.println("Directory found: " + directoryName);
                    boolean flag=false;
                    MetaDataPointer metaDataPointerInDB=metaDataPointerService.getMetaDataPointer(directoryName);

                    if(metaDataPointerInDB==null){
                        flag=true;
                    }
                    else if(metaDataPointerInDB.getStageStatus()==0){
                        flag=true;
                    }
                    else{
                        flag=false;
                    }
                    if(flag){
                        
                    // Get a reference to the directory
                    DataLakeDirectoryClient directoryClient = fileSystemClient.getDirectoryClient(directoryName);
                    System.out.println(directoryClient.getDirectoryPath());
                    // Retrieve and print the directory lease status and creation timestamp
                    MetaDataPointer metaDataPointer = loadMetaDataPointer(directoryClient, directoryName,storageAccountUrl,fileSystemName);

                    if (metaDataPointer != null) {
                        // Check if the target file exists in the directory
                        if (doesFileExist(directoryClient, targetFileName)) {
                            Set<String> tableNames = jDBCTemplateUtiltiy
                                    .getTableInFolder(metaDataPointer.getFolderName(), fileSystemName);
                            

                        for (String tableName : tableNames) {
                            if(!tableNamesInDB.contains(tableName)){
                                if(modelJsonDownloader.downloadFile()){
                                    parseModelJson.parseModelJson();
                                }
                            }    
                                loadFolderSyncStatus(metaDataPointer, tableName);
                            }
                        System.out.println("File " + targetFileName + " found in directory: " + directoryName);
                        metaDataPointer=updateMetaDataPointerStageToInProgress(metaDataPointer);
                        }
                    }
                }
                }
            }
        intialLoad.setStageendtime(LocalDateTime.now());
        intialLoad=initialLoadService.save(intialLoad);
        }
    }   
        Short pointerCopyStatus=1;
        TreeSet<MetaDataPointer> metaDataPointers=metaDataPointerService.getMetaDataPointerBystageStatus(pointerCopyStatus);
        for(MetaDataPointer metaDataPointer:metaDataPointers){
            startProcessing(metaDataPointer);
        }
        Short copyStatus=0;
        List<FolderSyncStatus> pendingTablesInFolder=folderSyncStatusService.getFolderSyncStatusBycopyStatus(copyStatus);
        if(pendingTablesInFolder.size() ==0){
            intialLoad.setEndtimestamp(LocalDateTime.now());
            intialLoad.setStagestatus(2);
            initialLoadService.save(intialLoad);
        }
    }

    // Method to check if a file exists in a specified directory
    private boolean doesFileExist(DataLakeDirectoryClient directoryClient, String fileName) {
        try {
            boolean flag = false;
            System.out.println(directoryClient.getDirectoryUrl());
            DataLakeFileClient fileClient = directoryClient.getFileClient(fileName);
            System.out.println(fileClient);
            if (fileClient != null) {
                System.out.println("file size" + fileClient.getProperties().getFileSize());
                if ((fileClient.getProperties().getFileSize() > 2000)) {
                    flag = true;
                }
            } else {
                flag = false;
            }

            return flag; // If properties are fetched, file exists
        } catch (Exception e) {
            // Handle cases where file doesn't exist or other exceptions occur
            return false;
        }

    }

    // Method to print the lease status and creation timestamp of a directory
    private MetaDataPointer loadMetaDataPointer(DataLakeDirectoryClient directoryClient, String directoryName,String storageAccountUrl,String fileSystemName) {
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
            metaDataPointer.setStageStartTime(LocalDateTime.now());
            metaDataPointer.setStageEndTime(null);            metaDataPointer.setStageStatus(copyStatus);
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
        folderSyncStatus=folderSyncStatusService.save(folderSyncStatus);
        return folderSyncStatus;
    }

    private MetaDataPointer updateMetaDataPointerStageToInProgress(MetaDataPointer metaDataPointer){
        Short copyStatus = 1;
        metaDataPointer.setStageStatus(copyStatus);
        metaDataPointer.setStageStartTime(LocalDateTime.now());
        metaDataPointer.setStageEndTime(null);
        metaDataPointer = metaDataPointerService.save(metaDataPointer);
        return metaDataPointer;
    }
    

    private void startProcessing(MetaDataPointer metaDataPointer){
        polybaseService.startSyncInFolder(metaDataPointer);

    }
}
