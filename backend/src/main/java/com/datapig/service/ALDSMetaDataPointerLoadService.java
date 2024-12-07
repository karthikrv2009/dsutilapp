package com.datapig.service;

import org.slf4j.LoggerFactory;
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
import com.datapig.entity.MetaDataCatlog;
import com.datapig.entity.MetaDataPointer;
import com.datapig.utility.JDBCTemplateUtiltiy;
import com.datapig.utility.ModelJsonDownloader;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ALDSMetaDataPointerLoadService {

    private static final Logger logger = LoggerFactory.getLogger(ALDSMetaDataPointerLoadService.class);

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

        intialLoad = initialLoadService.getIntialLoad("DBSynctUtilInitalLoad");
        if ((intialLoad.getStatus() == 0 || intialLoad.getStatus() == 1) && intialLoad.getStagestatus() != 2) {
            intialLoad.setStagestatus(1);
            intialLoad.setStatus(1);
            intialLoad.setStarttimestamp(LocalDateTime.now());
            intialLoad.setStagestarttime(LocalDateTime.now());
            intialLoad = initialLoadService.save(intialLoad);
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
                        logger.info("Directory found: {}", directoryName);
                        boolean flag = false;
                        MetaDataPointer metaDataPointerInDB = metaDataPointerService.getMetaDataPointer(directoryName);

                        if (metaDataPointerInDB == null) {
                            flag = true;
                        } else if (metaDataPointerInDB.getStageStatus() == 0) {
                            flag = true;
                        } else {
                            flag = false;
                        }
                        if (flag) {

                            // Get a reference to the directory
                            DataLakeDirectoryClient directoryClient = fileSystemClient
                                    .getDirectoryClient(directoryName);
                            logger.info(directoryClient.getDirectoryPath());
                            // Retrieve and print the directory lease status and creation timestamp
                            MetaDataPointer metaDataPointer = loadMetaDataPointer(directoryClient, directoryName,
                                    storageAccountUrl, fileSystemName);

                            if (metaDataPointer != null) {
                                // Check if the target file exists in the directory
                                if (doesFileExist(directoryClient, targetFileName)) {
                                    Set<String> tableNames = jDBCTemplateUtiltiy
                                            .getTableInFolder(metaDataPointer.getFolderName(), fileSystemName);

                                    for (String tableName : tableNames) {
                                        if (!tableNamesInDB.contains(tableName)) {
                                            if (modelJsonDownloader.downloadFile()) {
                                                parseModelJson.parseModelJson();
                                            }
                                        }
                                        loadFolderSyncStatus(metaDataPointer, tableName);
                                    }
                                    logger.info("File {} found in directory: {}", targetFileName, directoryName);
                                    metaDataPointer = updateMetaDataPointerStageToInProgress(metaDataPointer);
                                }
                            }
                        }
                    }
                }
            }
            intialLoad.setTotalpackages(metaDataPointerService.count());
            intialLoad.setStageendtime(LocalDateTime.now());
            intialLoad.setStagestatus(2);
            intialLoad = initialLoadService.save(intialLoad);
        }
        Short pointerCopyStatus = 1;
        TreeSet<MetaDataPointer> metaDataPointers = metaDataPointerService
                .getMetaDataPointerBystageStatus(pointerCopyStatus);
        for (MetaDataPointer metaDataPointer : metaDataPointers) {
            startProcessing(metaDataPointer);
        }
        Short copyStatus = 0;
        List<FolderSyncStatus> pendingTablesInFolder = folderSyncStatusService
                .getFolderSyncStatusBycopyStatus(copyStatus);
        if (pendingTablesInFolder.size() == 0) {
            intialLoad.setEndtimestamp(LocalDateTime.now());
            intialLoad.setStatus(2);
            initialLoadService.save(intialLoad);
        }
    }

    // Method to check if a file exists in a specified directory
    private boolean doesFileExist(DataLakeDirectoryClient directoryClient, String fileName) {
        try {
            boolean flag = false;
            logger.info(directoryClient.getDirectoryUrl());
            DataLakeFileClient fileClient = directoryClient.getFileClient(fileName);
            if (fileClient != null) {
                logger.debug("File client: {}", fileClient.toString());
            } else {
                logger.debug("File client is null");
            }
            if (fileClient != null) {
                logger.info("file size", fileClient.getProperties().getFileSize());
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
            logger.info("  Creation Time: ", (creationTime != null ? creationTime : "Unknown"));
        } catch (Exception e) {
            logger.info("  Failed to retrieve properties for directory: ", directoryClient.getDirectoryPath());
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

    private MetaDataPointer updateMetaDataPointerStageToInProgress(MetaDataPointer metaDataPointer) {
        Short copyStatus = 1;
        metaDataPointer.setStageStatus(copyStatus);
        metaDataPointer.setStageStartTime(LocalDateTime.now());
        metaDataPointer.setStageEndTime(null);
        metaDataPointer = metaDataPointerService.save(metaDataPointer);
        return metaDataPointer;
    }

    private TreeSet<MetaDataPointer> errorHandle() {
        List<MetaDataPointer> metaDataPointerList = new ArrayList<MetaDataPointer>();
        TreeSet<MetaDataPointer> orderedSet = null;
        short failStatus = 3;
        List<MetaDataCatlog> metaDataCatlogs = metaDataCatlogService.findBylastCopyStatus(failStatus);
        for (MetaDataCatlog failMetaDataCatlog : metaDataCatlogs) {
            if (failMetaDataCatlog.getRetry() == 3) {
                quarintineTable(failMetaDataCatlog);
            }
            if ((failMetaDataCatlog.getQuarintine() != 1) && (failMetaDataCatlog.getRetry() <= 3)) {
                MetaDataPointer metaDataPointer = metaDataPointerService
                        .getMetaDataPointer(failMetaDataCatlog.getLastUpdatedFolder());
                if (metaDataPointer != null) {
                    updateErrorTableToStart(failMetaDataCatlog, metaDataPointer);
                }
                metaDataPointerList.add(metaDataPointer);
            }

        }
        if (metaDataPointerList != null) {
            // Create a TreeSet with a custom comparator for stageStartTime
            orderedSet = new TreeSet<>(Comparator.comparing(MetaDataPointer::getStageStartTime));
            orderedSet.addAll(metaDataPointerList);
        }
        return orderedSet;
    }

    private void quarintineTable(MetaDataCatlog metaDataCatlog) {
        int quarintine = 1;
        metaDataCatlog.setQuarintine(quarintine);
        metaDataCatlogService.save(metaDataCatlog);
    }

    private void updateErrorTableToStart(MetaDataCatlog metaDataCatlog, MetaDataPointer metaDataPointer) {
        FolderSyncStatus folderSyncStatus = folderSyncStatusService.getFolderSyncStatusOnFolderAndTableName(
                metaDataPointer.getFolderName(), metaDataCatlog.getTableName());
        if (folderSyncStatus != null) {
            Short copyStatus = 0;
            folderSyncStatus.setCopyStatus(copyStatus);
            folderSyncStatusService.save(folderSyncStatus);
        }
        if (metaDataCatlog != null) {
            Short copyStatus = 1;
            metaDataCatlog.setLastCopyStatus(copyStatus);
            int retry = metaDataCatlog.getRetry();
            retry = retry + 1;
            metaDataCatlog.setRetry(retry);
            metaDataCatlogService.save(metaDataCatlog);
        }
    }

    private void startProcessing(MetaDataPointer metaDataPointer) {
        // Retry Error logic
        TreeSet<MetaDataPointer> failedMetaDataPointers = errorHandle();
        for (MetaDataPointer failMetaDataPointer : failedMetaDataPointers) {
            polybaseService.startSyncInFolder(failMetaDataPointer);
        }
        polybaseService.startSyncInFolder(metaDataPointer);

    }
}
