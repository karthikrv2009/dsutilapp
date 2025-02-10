package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClientBuilder;
import com.azure.storage.file.datalake.models.PathProperties;
import com.datapig.component.BulkLoadErrorHandler;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.HealthMetrics;
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
    private BulkLoadErrorHandler bulkLoadErrorHandler;

    @Autowired
    private HealthMetricsService healthMetricsService;

    @Autowired
    private PolybaseService polybaseService;

    @Autowired
    private MetaDataCatlogService metaDataCatlogService;

    public void startParse(String folderName, DatabaseConfig databaseConfig) {

        Set<String> tableNamesInMetadataCatalogDB = metaDataCatlogService.getAllTableNamesByDbIdentifier(databaseConfig.getDbIdentifier());
        String dbIdentifier=databaseConfig.getDbIdentifier();
        String fileSystemName = databaseConfig.getAdlsContainerName();
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
                        .getTableInFolder(metaDataPointer.getFolderName(), fileSystemName,dbIdentifier);

                for (String tableName : tableNamesInAdls) {
                    if (!tableNamesInMetadataCatalogDB.contains(tableName)) {
                        if (modelJsonDownloader.downloadFile(databaseConfig.getDbIdentifier())) {
                            boolean forCDC=false;
                            parseModelJson.parseModelJson(dbIdentifier,forCDC);
                        }
                    }
                    if (!tablesInDB.contains(tableName)) {
                        loadFolderSyncStatus(metaDataPointer, tableName, dbIdentifier);
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
                    fileSystemName,dbIdentifier);
            if (doesFileExist(directoryClient, targetFileName)) {

                Set<String> tableNamesInAdls = jdbcTemplateUtiltiy
                        .getTableInFolder(metaDataPointer.getFolderName(), fileSystemName,dbIdentifier);
                for (String tableName : tableNamesInAdls) {
                    loadFolderSyncStatus(metaDataPointer, tableName, dbIdentifier);
                }
                updateChangeLogToReady(metaDataPointer); // Update the status after processing
            }
        }

        // Retry Error logic
        TreeSet<MetaDataPointer> failedMetaDataPointers = errorHandle(dbIdentifier);;
        for (MetaDataPointer metaDataPointer : failedMetaDataPointers) {
            polybaseService.startSyncInFolder(metaDataPointer,databaseConfig);
        }

        TreeSet<MetaDataPointer> metaDataPointers = existingFolderStagedNotComplete(databaseConfig.getDbIdentifier());
        for (MetaDataPointer metaDataPointer : metaDataPointers) {
            polybaseService.startSyncInFolder(metaDataPointer,databaseConfig);
        }
    }

    private TreeSet<MetaDataPointer> errorHandle(String dbIdentifier) {
        List<MetaDataPointer> metaDataPointerList = new ArrayList<>();
        TreeSet<MetaDataPointer> orderedSet = null;
        short failStatus = 3;
        List<MetaDataCatlog> metaDataCatlogs = metaDataCatlogService.findBylastCopyStatusAndDbIdentifier(failStatus,
                dbIdentifier);
        
        for (MetaDataCatlog failMetaDataCatlog : metaDataCatlogs) {
            List<HealthMetrics> nhealthMetrics=healthMetricsService.findByfolderNameAndDbIdentifierAndTableNameAndStatus(failMetaDataCatlog.getLastUpdatedFolder(), failMetaDataCatlog.getDbIdentifier(),failMetaDataCatlog.getTableName(),2);
            for(HealthMetrics healthMetrics:nhealthMetrics){
                bulkLoadErrorHandler.fixTruncateError(healthMetrics);
            }
            
            if (failMetaDataCatlog.getRetry() == 3) {
                failMetaDataCatlog=quarintineTable(failMetaDataCatlog);
            }
            if ((failMetaDataCatlog.getQuarintine() != 1) && (failMetaDataCatlog.getRetry() <= 3)) {
                System.out.println("Input :"+failMetaDataCatlog.getLastUpdatedFolder()+"==>"+ dbIdentifier);
                MetaDataPointer metaDataPointer = metaDataPointerService
                        .getMetaDataPointerBydbIdentifierAndFolder(dbIdentifier,failMetaDataCatlog.getLastUpdatedFolder());
                                System.out.println("MeteDataPointer ==>"+metaDataPointer.getFolderName());
                
                if (metaDataPointer != null) {
                    failMetaDataCatlog=updateErrorTableToStart(failMetaDataCatlog, metaDataPointer, dbIdentifier);
                    metaDataPointerList.add(metaDataPointer);
                }
                
            }

        }
        if (metaDataPointerList != null) {
            System.out.println("metaDataPointerList size: " + metaDataPointerList.size());
            for (MetaDataPointer pointer : metaDataPointerList) {
                if (pointer == null) {
                    System.out.println("Found a null element in metaDataPointerList");
                } else if (pointer.getStageStartTime() == null) {
                    System.out.println("Found an element with null stageStartTime");
                }
            }
            orderedSet = new TreeSet<>(Comparator.comparing(MetaDataPointer::getStageStartTime));
            orderedSet.addAll(metaDataPointerList);
        } 
        
        return orderedSet;
    }

    private MetaDataCatlog quarintineTable(MetaDataCatlog metaDataCatlog) {
        int quarintine = 1;
        metaDataCatlog.setQuarintine(quarintine);
        metaDataCatlog=metaDataCatlogService.save(metaDataCatlog);
        return metaDataCatlog;
    }

    private MetaDataCatlog updateErrorTableToStart(MetaDataCatlog metaDataCatlog, MetaDataPointer metaDataPointer,
            String dbIdentifier) {
        MetaDataCatlog metaDataCatlog2=null;
        FolderSyncStatus folderSyncStatus = folderSyncStatusService
                .getFolderSyncStatusOnFolderAndTableNameAndDBIdentifier(
                        metaDataPointer.getFolderName(), metaDataCatlog.getTableName(), dbIdentifier);
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
            metaDataCatlog2=metaDataCatlogService.save(metaDataCatlog);
        }
        return metaDataCatlog2;
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
            String storageAccountUrl, String fileSystemName, String dbIdentifier) {
        MetaDataPointer metaDataPointer = null;
        try {
            PathProperties properties = directoryClient.getProperties();
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
            metaDataPointer.setDbIdentifier(dbIdentifier);
            metaDataPointer = metaDataPointerService.save(metaDataPointer);
            logger.info("  Creation Time: {}", (creationTime != null ? creationTime : "Unknown"));
        } catch (Exception e) {
            logger.info("  Failed to retrieve properties for directory: {}", directoryClient.getDirectoryPath());
        }
        return metaDataPointer;
    }

    private FolderSyncStatus loadFolderSyncStatus(MetaDataPointer metaDataPointer, String tableName,
            String dbIdentifier) {
        FolderSyncStatus folderSyncStatus = null;
        Short copyStatus = 0;
        MetaDataCatlog metaDataCatlog= metaDataCatlogService.getMetaDataCatlogByTableNameAndDbIdentifier(tableName, dbIdentifier);
        if(metaDataCatlog!=null){
            folderSyncStatus = new FolderSyncStatus();
            folderSyncStatus.setFolder(metaDataPointer.getFolderName());
            folderSyncStatus.setTableName(metaDataCatlog.getTableName());
            folderSyncStatus.setCopyStatus(copyStatus);
            folderSyncStatus.setDbIdentifier(dbIdentifier);
            folderSyncStatus.setArchived(0);
            folderSyncStatus = folderSyncStatusService.save(folderSyncStatus);
        }
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
