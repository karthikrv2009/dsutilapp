package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder;
import com.azure.storage.file.datalake.models.PathProperties;
import com.datapig.entity.DatabaseConfig;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SynapseLogParserService {

    private static final Logger logger = LoggerFactory.getLogger(SynapseLogParserService.class);

    @Autowired
    private ParseModelJson parseModelJson;

    @Autowired
    private ModelJsonDownloader modelJsonDownloader;

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

    public void startParse(String folderName, DatabaseConfig databaseConfig) {

        Set<String> tableNamesInMetadataCatalogDB = metaDataCatlogService.getAllTableNamesByDbIdentifier(databaseConfig.getDbIdentifier());
        String fileSystemName = databaseConfig.getAdlsStorageAccountName();
        String targetFileName = databaseConfig.getAdlsCdmFileName();

        String storageAccountUrl = databaseConfig.getAdlsStorageAccountEndpoint();

        String saskey = databaseConfig.getAdlsStorageAccountSasKey();

        // ADLS Gen2 endpoint with SAS token
        String endpointWithSAS = storageAccountUrl + "/" + fileSystemName + "/?" + saskey;

        // Create a DataLakeFileSystemClient using SAS token
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
                .endpoint(endpointWithSAS)
                .fileSystemName(fileSystemName)
                .buildClient();

        // Existing Folder but not staged in FolderSyncStatus
        TreeSet<MetaDataPointer> existingFoldersNotStaged = existingFolderNotStaged(databaseConfig.getDbIdentifier());

        for (MetaDataPointer metaDataPointer : existingFoldersNotStaged) {
            // Get a reference to the directory
            DataLakeDirectoryClient directoryClient = fileSystemClient.getDirectoryClient(folderName);
            logger.info(directoryClient.getDirectoryPath());
            if (doesFileExist(directoryClient, targetFileName)) {
                List<String> tablesInDB = getTablesPerFolderInDB(metaDataPointer);
                Set<String> tableNamesInAdls = jdbcTemplateUtiltiy
                        .getTableInFolder(metaDataPointer.getFolderName(), fileSystemName);

                for (String tableName : tableNamesInAdls) {
                    if (!tableNamesInMetadataCatalogDB.contains(tableName)) {
                        if (modelJsonDownloader.downloadFile(databaseConfig.getDbIdentifier())) {
                            parseModelJson.parseModelJson(metaDataPointer.getDbIdentifier());
                        }
                    }
                    if (!tablesInDB.contains(tableName)) {
                        loadFolderSyncStatus(metaDataPointer, tableName);
                    }
                }
                updateChangeLogToReady(metaDataPointer); // Update the status after processing
            }
        }

        MetaDataPointer metaDataPointerInDB = metaDataPointerService
                .getMetaDataPointerBydbIdentifierAndFolder(databaseConfig.getDbIdentifier(), folderName);

        if (metaDataPointerInDB == null) {
            DataLakeDirectoryClient directoryClient = fileSystemClient.getDirectoryClient(folderName);
            MetaDataPointer metaDataPointer = loadMetaDataPointer(directoryClient, folderName, storageAccountUrl,
                    fileSystemName);
            if (doesFileExist(directoryClient, targetFileName)) {

                Set<String> tableNamesInAdls = jdbcTemplateUtiltiy
                        .getTableInFolder(metaDataPointer.getFolderName(), fileSystemName);
                for (String tableName : tableNamesInAdls) {
                    loadFolderSyncStatus(metaDataPointer, tableName);
                }
                updateChangeLogToReady(metaDataPointer); // Update the status after processing
            }
        }

        // Retry Error logic
        TreeSet<MetaDataPointer> failedMetaDataPointers = errorHandle();
        for (MetaDataPointer metaDataPointer : failedMetaDataPointers) {
            polybaseService.startSyncInFolder(metaDataPointer,databaseConfig);
        }

        TreeSet<MetaDataPointer> metaDataPointers = existingFolderStagedNotComplete(databaseConfig.getDbIdentifier());
        for (MetaDataPointer metaDataPointer : metaDataPointers) {
            polybaseService.startSyncInFolder(metaDataPointer,databaseConfig);
        }
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

    // Method to get Table per folder from FolderSyncStatus in DB
    private List<String> getTablesPerFolderInDB(MetaDataPointer metaDataPointer) {
        List<String> tables = new ArrayList<String>();
        List<FolderSyncStatus> folderSyncStatuss = folderSyncStatusService
                .getFolderSyncStatusByfolderAndDbIdentifier(metaDataPointer.getFolderName(),
                        metaDataPointer.getDbIdentifier());
        for (FolderSyncStatus folderSyncStatus : folderSyncStatuss) {
            tables.add(folderSyncStatus.getTableName());
        }
        return tables;
    }

    // Method to check if a file exists in a specified directory
    private boolean doesFileExist(DataLakeDirectoryClient directoryClient, String fileName) {
        try {
            boolean flag = false;
            logger.info(directoryClient.getDirectoryUrl());
            DataLakeFileClient fileClient = directoryClient.getFileClient(fileName);
            if (logger.isInfoEnabled()) {
                logger.info(fileClient.toString());
            }
            while (!flag) {
                Thread.sleep(10000);
                if (fileClient != null) {
                    logger.info("file size" + fileClient.getProperties().getFileSize());
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
            logger.info("  Creation Time: {}", (creationTime != null ? creationTime : "Unknown"));
        } catch (Exception e) {
            logger.error("Failed to retrieve properties for directory: " + directoryClient.getDirectoryPath(),
                    e.getMessage(), e);
        }
        return metaDataPointer;
    }

    private FolderSyncStatus loadFolderSyncStatus(MetaDataPointer metaDataPointer, String tableName) {
        Short copyStatus = 0;
        FolderSyncStatus folderSyncStatus = new FolderSyncStatus();
        folderSyncStatus.setFolder(metaDataPointer.getFolderName());
        folderSyncStatus.setTableName(tableName);
        folderSyncStatus.setCopyStatus(copyStatus);
        folderSyncStatus.setDbIdentifier(metaDataPointer.getDbIdentifier());
        folderSyncStatus = folderSyncStatusService.save(folderSyncStatus);
        return folderSyncStatus;
    }

    private void updateChangeLogToReady(MetaDataPointer metaDataPointer) {
        Short stageStatus = 1;
        metaDataPointer.setStageStartTime(LocalDateTime.now());
        metaDataPointer.setStageStatus(stageStatus);
        metaDataPointerService.save(metaDataPointer);
    }

    private TreeSet<MetaDataPointer> existingFolderNotStaged(String dbIdentifier) {
        Short copyStatus = 0;
        TreeSet<MetaDataPointer> metaDataPointers = metaDataPointerService
                .getMetaDataPointerBystageStatusandDbidentifier(copyStatus, dbIdentifier);
        return metaDataPointers;
    }

    private TreeSet<MetaDataPointer> existingFolderStagedNotComplete(String dbIdentifier) {
        Short copyStatus = 1;
        TreeSet<MetaDataPointer> metaDataPointers = metaDataPointerService
                .getMetaDataPointerBystageStatusandDbidentifier(copyStatus, dbIdentifier);
        return metaDataPointers;
    }

}
