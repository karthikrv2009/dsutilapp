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
import com.datapig.entity.MetaDataCatlog;
import com.datapig.entity.MetaDataPointer;
import com.datapig.utility.JDBCTemplateUtiltiy;
import com.datapig.utility.ModelJsonDownloader;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class SynapseLogParserService {

    @Autowired
    private ParseModelJson parseModelJson;

    @Autowired
    private ModelJsonDownloader modelJsonDownloader;


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

    @Autowired
    private MetaDataCatlogService metaDataCatlogService;

    public void startParse(String folderName) {

        Set<String> tableNamesInMetadataCatalogDB = metaDataCatlogService.getAllTableName();            
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
                    if(!tableNamesInMetadataCatalogDB.contains(tableName)){
                        if(modelJsonDownloader.downloadFile()){
                            parseModelJson.parseModelJson();
                        }
                    }   
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

        //Retry Error logic
        TreeSet<MetaDataPointer> failedMetaDataPointers= errorHandle();
        for(MetaDataPointer metaDataPointer:failedMetaDataPointers){
            polybaseService.startSyncInFolder(metaDataPointer);
        }

        TreeSet<MetaDataPointer> metaDataPointers=existingFolderStagedNotComplete(); 
        for(MetaDataPointer metaDataPointer:metaDataPointers){
            polybaseService.startSyncInFolder(metaDataPointer);
        }
    }

    private TreeSet<MetaDataPointer> errorHandle(){
        List<MetaDataPointer> metaDataPointerList=new ArrayList<MetaDataPointer>();
        TreeSet<MetaDataPointer> orderedSet =null;
        short failStatus=3;
        List<MetaDataCatlog> metaDataCatlogs=metaDataCatlogService.findBylastCopyStatus(failStatus);
        for(MetaDataCatlog failMetaDataCatlog:metaDataCatlogs){
            if(failMetaDataCatlog.getRetry()==3){
                quarintineTable(failMetaDataCatlog);
            }
            if((failMetaDataCatlog.getQuarintine()!=1) && (failMetaDataCatlog.getRetry()<=3)){
                MetaDataPointer metaDataPointer=metaDataPointerService.getMetaDataPointer(failMetaDataCatlog.getLastUpdatedFolder());
                if(metaDataPointer!=null){
                    updateErrorTableToStart(failMetaDataCatlog,metaDataPointer);
                }
                metaDataPointerList.add(metaDataPointer);
            }
            
        }
        if(metaDataPointerList!=null){
            // Create a TreeSet with a custom comparator for stageStartTime
            orderedSet = new TreeSet<>(Comparator.comparing(MetaDataPointer::getStageStartTime));
            orderedSet.addAll(metaDataPointerList);
        }
        return orderedSet;
    }

    private void quarintineTable(MetaDataCatlog metaDataCatlog){
            int quarintine=1;
            metaDataCatlog.setQuarintine(quarintine);
            metaDataCatlogService.save(metaDataCatlog);
    }
    private void updateErrorTableToStart(MetaDataCatlog metaDataCatlog,MetaDataPointer metaDataPointer){
        FolderSyncStatus folderSyncStatus=folderSyncStatusService.getFolderSyncStatusOnFolderAndTableName(metaDataPointer.getFolderName(), metaDataCatlog.getTableName());
        if(folderSyncStatus!=null){
            Short copyStatus=0;
            folderSyncStatus.setCopyStatus(copyStatus);
            folderSyncStatusService.save(folderSyncStatus);
        }
        if(metaDataCatlog!=null){
            Short copyStatus=1;
            metaDataCatlog.setLastCopyStatus(copyStatus);
            int retry=metaDataCatlog.getRetry();
            retry=retry+1;
            metaDataCatlog.setRetry(retry);
            metaDataCatlogService.save(metaDataCatlog);
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
            metaDataPointer.setStageStartTime(LocalDateTime.now());
            metaDataPointer.setStageEndTime(null);
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
        metaDataPointer.setStageStartTime(LocalDateTime.now());
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
