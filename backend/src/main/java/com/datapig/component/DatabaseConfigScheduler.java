package com.datapig.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.datapig.entity.ArchivedFolder;
import com.datapig.entity.ChangeDataTracking;
import com.datapig.entity.ChangeDataTrackingCatalog;
import com.datapig.entity.ChangeDataTrackingPointer;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.HealthMetrics;
import com.datapig.entity.IntialLoad;
import com.datapig.entity.MetaDataCatlog;
import com.datapig.entity.MetaDataPointer;
import com.datapig.service.ArchivedFolderService;

import com.datapig.service.ChangeDataTrackingCatalogService;
import com.datapig.service.ChangeDataTrackingPointerService;
import com.datapig.service.ChangeDataTrackingService;
import com.datapig.service.DatabaseConfigService;
import com.datapig.service.FolderSyncStatusService;
import com.datapig.service.HealthMetricsService;
import com.datapig.service.InitialLoadService;
import com.datapig.service.MetaDataCatlogService;
import com.datapig.service.MetaDataPointerService;
import com.datapig.service.PipelineService;
import com.datapig.utility.ArchiveToHotRehydration;
import com.datapig.utility.JDBCTemplateUtiltiy;
import com.datapig.utility.PurgeADLSFiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


@Component
public class DatabaseConfigScheduler {

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Autowired
    private ArchivedFolderService archivedFolderService;

    @Autowired
    private MetaDataPointerService metaDataPointerService;

    @Autowired
    private FolderSyncStatusService folderSyncStatusService;

    @Autowired
    private ChangeDataTrackingService changeDataTrackingService;

    @Autowired
    private ChangeDataTrackingPointerService changeDataTrackingPointerService;

    @Autowired
    private ChangeDataTrackingCatalogService changeDataTrackingCatalogService;

    @Autowired
    private ArchiveToHotRehydration archiveToHotRehydration;

    @Autowired
    JDBCTemplateUtiltiy jdbcTemplateUtiltiy;

    @Autowired
    MetaDataCatlogService metaDataCatlogService;

    @Autowired
    private BulkLoadErrorHandler bulkLoadErrorHandler;

    @Autowired
    private HealthMetricsService healthMetricsService;

    @Autowired
    private PurgeADLSFiles purgeADLSFiles;

    @Autowired
    private InitialLoadService initialLoadService;

    @Autowired
    private PipelineService pipelineService;

    @Scheduled(fixedRate = 60000) // 300000 milliseconds = 5 minutes
    public void processDatabaseConfigs() {
        List<DatabaseConfig> configs = databaseConfigService.getAllDatabaseConfigs();
        for (DatabaseConfig config : configs) {
            if (config.isEnableArchive()) {
                List<MetaDataPointer> metaDataPointersNotArchived = archivedFolderService
                        .listMetaDataPoicnterArchived(config.getDbIdentifier());
                for (MetaDataPointer metaDataPointer : metaDataPointersNotArchived) {
                    createArchivedFolder(metaDataPointer);
                }
                List<ArchivedFolder> archivedFoldersNotArchived = archivedFolderService
                        .findByStageStatusAndDbIdentifier(0, config.getDbIdentifier());
                for (ArchivedFolder archivedFolder : archivedFoldersNotArchived) {
                    MetaDataPointer metaDataPointer = metaDataPointerService.getMetaDataPointerBydbIdentifierAndFolder(
                            archivedFolder.getFolderName(), config.getDbIdentifier());
                    Short copyStatus = 1;
                    List<FolderSyncStatus> lstFolderSync = folderSyncStatusService
                            .findByDbIdentifierAndFolderAndCopyStatusAndArcarchived(config.getDbIdentifier(),
                                    metaDataPointer.getFolderName(), copyStatus, 0);
                    for (FolderSyncStatus folder : lstFolderSync) {
                        System.out.println(
                                "Folder to process::" + folder.getTableName() + "==>" + folder.getCopyStatus());
                        String path = folder.getFolder() + "/" + folder.getTableName() + "/";
                        boolean flag1 = moveBlobsToArchive(config, path);
                        if (flag1) {
                            folder.setArchived(1);
                            folderSyncStatusService.save(folder);
                        }
                    }
                    List<FolderSyncStatus> lstfolderSyncStatus = folderSyncStatusService
                            .findByDbIdentifierAndFolderAndArchived(metaDataPointer.getDbIdentifier(),
                                    metaDataPointer.getFolderName(), 0);
                    if (lstfolderSyncStatus != null && lstfolderSyncStatus.size() == 0) {
                        updateArchivedFolder(archivedFolder);
                    } else if (lstfolderSyncStatus == null) {
                        updateArchivedFolder(archivedFolder);
                    } else {
                        System.out.println("The list is not empty." + lstfolderSyncStatus.size());
                    }
                }
            }
        }
    }

    private void createArchivedFolder(MetaDataPointer metaDataPointer) {
        ArchivedFolder archivedFolder = new ArchivedFolder();
        if (metaDataPointer != null) {
            archivedFolder.setDbIdentifier(metaDataPointer.getDbIdentifier());
            archivedFolder.setFolderName(metaDataPointer.getFolderName());
            archivedFolder.setAdlsarchivetimestamp(LocalDateTime.now());
            archivedFolder.setStageStatus(0);
        }
        archivedFolderService.save(archivedFolder);
    }

    private void updateArchivedFolder(ArchivedFolder archivedFolder) {
        if (archivedFolder != null) {
            archivedFolder.setAdlsarchivetimestamp(LocalDateTime.now());
            archivedFolder.setStageStatus(1);
            archivedFolderService.save(archivedFolder);
        }
    }

    private boolean moveBlobsToArchive(DatabaseConfig config, String folderPath) {
        System.out.println("Trying folder path:" + folderPath);
        boolean flag = false;
        String storageAccountUrl = config.getAdlsStorageAccountEndpoint();
        String sasToken = config.getAdlsStorageAccountSasKey();
        String containerName = config.getAdlsContainerName();
        String baseFolderPath = folderPath;
        // metaDataPointerService.get
        try {
            // Create a BlobServiceClient using the storage account URL and SAS token
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(storageAccountUrl)
                    .sasToken(sasToken)
                    .buildClient();

            // Get the container client
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Iterate through all blobs under the specified folder (prefix)
            PagedIterable<BlobItem> blobs = containerClient.listBlobsByHierarchy(baseFolderPath);

            for (BlobItem blobItem : blobs) {
                String blobName = blobItem.getName();

                // Log current blob being processed
                System.out.println("Processing blob: " + blobName);

                // Get the blob client
                BlobClient blobClient = containerClient.getBlobClient(blobName);
                // Check if the blob is not a folder (assuming folders end with '/')
                if (!blobName.endsWith("/")) {
                    BlobItemProperties properties = blobItem.getProperties();
                    if (properties != null && properties.getAccessTier() != null) {
                        if (properties.getAccessTier() == AccessTier.HOT) {
                            // Set the access tier to Archive
                            blobClient.setAccessTier(AccessTier.ARCHIVE);
                            System.out.println("Blob '" + blobName + "' moved to Archive tier successfully.");
                        }
                    }
                }

                flag = true;
            }
        } catch (BlobStorageException e) {
            System.out.println("Error moving blobs to Archive tier: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An error occurred while processing blobs: " + e.getMessage());
            e.printStackTrace();
        }
        return flag;
    }

    @Scheduled(fixedRate = 60000) // 300000 milliseconds = 5 minutes
    public void checkIfFolderArchivable() {
        List<DatabaseConfig> databaseConfigs = databaseConfigService.getAllDatabaseConfigs();

        for (DatabaseConfig databaseConfig : databaseConfigs) {
            int rehydrationInProgress = 0;
            List<ChangeDataTracking> changeDataTrackings = changeDataTrackingService
                    .findByStageStatusAndDbIdentifier(rehydrationInProgress, databaseConfig.getDbIdentifier());
            for (ChangeDataTracking changeDataTracking : changeDataTrackings) {
                int rehydrationStatus = 0;
                List<ChangeDataTrackingPointer> pointers = changeDataTrackingPointerService
                        .findByCdcTableNameAndDbIdentifierAndRehydrationStatus(changeDataTracking.getCdcTableName(),
                                databaseConfig.getDbIdentifier(), rehydrationStatus);
                for (ChangeDataTrackingPointer changeDataTrackingPointer : pointers) {
                    String path = changeDataTrackingPointer.getFolderName();
                    if (changeDataTrackingPointer.getFolderName().contains("/model.json")) {
                        boolean flag = archiveToHotRehydration
                                .rehydrateBlobToHotTier(databaseConfig.getAdlsContainerName(), path, databaseConfig);
                        if (flag) {
                            updateRehydrationToStart(changeDataTrackingPointer);
                        }
                    } else {
                        System.out.println("scheduler call==>"+path);
                        boolean flag = archiveToHotRehydration.rehydrateToHotTier(databaseConfig.getAdlsContainerName(),
                                path, databaseConfig);
                        if (flag) {
                            updateRehydrationToStart(changeDataTrackingPointer);
                        }

                    }
                }
                rehydrationStatus = 1;
                List<ChangeDataTrackingPointer> pointersStarted = changeDataTrackingPointerService
                        .findByCdcTableNameAndDbIdentifierAndRehydrationStatus(changeDataTracking.getCdcTableName(),
                                databaseConfig.getDbIdentifier(), rehydrationStatus);
                for (ChangeDataTrackingPointer changeDataTrackingPointer : pointersStarted) {
                    String path = changeDataTrackingPointer.getFolderName();
                    if (changeDataTrackingPointer.getFolderName().contains("/model.json")) {
                        boolean flag = archiveToHotRehydration.checkRehydrationStatusForBlob(
                                databaseConfig.getAdlsContainerName(), path, databaseConfig);
                        if (flag) {
                            changeDataTrackingPointer = updateRehydrationToComplete(changeDataTrackingPointer);
                            changeDataTrackingPointer = updateStageStatusToComplete(changeDataTrackingPointer);
                        }
                    } else {
                        boolean flag = archiveToHotRehydration
                                .checkRehydrationStatus(databaseConfig.getAdlsContainerName(), path, databaseConfig);
                        if (flag) {
                            changeDataTrackingPointer = updateRehydrationToComplete(changeDataTrackingPointer);
                            // changeDataTrackingPointer=updateStageStatusToComplete(changeDataTrackingPointer);
                        }
                    }
                }

                List<ChangeDataTrackingPointer> pointersPerCDCTable = changeDataTrackingPointerService
                        .findByCdcTableNameAndDbIdentifier(changeDataTracking.getCdcTableName(),
                                databaseConfig.getDbIdentifier());
                boolean rehydrateState = true;
                for (ChangeDataTrackingPointer changeDataTrackingPointer : pointersPerCDCTable) {
                    if (changeDataTrackingPointer.getRehydrationStatus() != 2) {
                        rehydrateState = false;
                    }
                }
                if (rehydrateState) {
                    // setting to ready
                    updateStageStatusCDC(changeDataTracking, 1);
                }
            }
            int readyToProcess = 1;
            List<ChangeDataTracking> changeDataTrackings2 = changeDataTrackingService
                    .findByStageStatusAndDbIdentifier(readyToProcess, databaseConfig.getDbIdentifier());
            for (ChangeDataTracking changeDataTracking : changeDataTrackings2) {
                boolean masterFlag = true;
                ChangeDataTrackingCatalog changeDataTrackingCatalog = changeDataTrackingCatalogService
                        .findbyCdcTableNameAndDbIdentifier(changeDataTracking.getCdcTableName(),
                                changeDataTracking.getDbIdentifier());
                if (changeDataTrackingCatalog != null) {
                    List<ChangeDataTrackingPointer> pointers = changeDataTrackingPointerService
                            .findByCdcTableNameAndDbIdentifier(changeDataTracking.getCdcTableName(),
                                    databaseConfig.getDbIdentifier());
                    for (ChangeDataTrackingPointer changeDataTrackingPointer : pointers) {
                        if (!changeDataTrackingPointer.getFolderName().contains("/model.json")) {
                            boolean flag = false;
                            flag = jdbcTemplateUtiltiy.stageCDCDataFromADLS(databaseConfig.getDbIdentifier(),
                                    databaseConfig.getAdlsContainerName(), changeDataTrackingPointer.getFolderName(),
                                    changeDataTrackingPointer.getCdcTableName(),
                                    changeDataTrackingCatalog.getDataFrame(),
                                    changeDataTrackingCatalog.getSelectColumn(), changeDataTracking.getTableName());
                            if (flag) {
                                changeDataTrackingPointer = updateStageStatusToComplete(changeDataTrackingPointer);

                            } else {
                                masterFlag = false;
                                changeDataTrackingPointer = updateStageStatusToFail(changeDataTrackingPointer);
                            }

                        }

                    }

                }
                if (masterFlag) {
                    // success
                    changeDataTracking = updateStageStatusCDC(changeDataTracking, 2);
                } else {
                    updateStageStatusCDC(changeDataTracking, 3);
                }
            }

            List<ChangeDataTrackingPointer> changeDataTrackingPointers = changeDataTrackingPointerService
                    .findByDbIdentifierAndStageStatus(databaseConfig.getDbIdentifier(), 1);
            for (ChangeDataTrackingPointer changeDataTrackingPointer : changeDataTrackingPointers) {
                int count = changeDataTrackingPointerService.readyToArchive(changeDataTrackingPointer.getDbIdentifier(),
                        changeDataTrackingPointer.getFolderName());
                if (count == 0) {
                    String tableName = extractTableName(changeDataTrackingPointer.getCdcTableName());
                    String folderName = getBaseFolderName(changeDataTrackingPointer.getFolderName());
                    System.out.println("folder Sync table name:" + tableName);
                    System.out.println("folder Sync folder name:" + folderName);
                    if (tableName != null) {
                        boolean archive = false;
                        FolderSyncStatus folderSyncStatus = folderSyncStatusService
                                .getFolderSyncStatusOnFolderAndTableNameAndDBIdentifier(folderName,
                                        tableName, changeDataTrackingPointer.getDbIdentifier());
                        if (folderSyncStatus != null) {
                            folderSyncStatus.setArchived(0);
                            folderSyncStatusService.save(folderSyncStatus);
                            archive = true;
                        }
                        if (archive) {
                            ArchivedFolder archivedFolder = archivedFolderService.findByFolderNameAndDbIdentifier(
                                    folderName, changeDataTrackingPointer.getDbIdentifier());
                            archivedFolderService.delete(archivedFolder);
                            changeDataTrackingPointer.setStageStatus(2);
                            changeDataTrackingPointerService.save(changeDataTrackingPointer);
                        }

                    }
                }
            }
        }
    }

    private static String getBaseFolderName(String input) {
        String[] parts = input.split("/");
        return parts[0];
    }

    private static String extractTableName(String input) {
        // Define the prefix and suffix separator
        String prefix = "cdc_";
        char separator = '_';
    
        // Calculate the length of the prefix
        int prefixLength = prefix.length();
    
        // Find the index of the last occurrence of the separator
        int separatorIndex = input.lastIndexOf(separator);
    
        // Check if the separator is placed correctly after the prefix
        if (separatorIndex > prefixLength) {
            // Extract and return the table name
            return input.substring(prefixLength, separatorIndex);
        } else {
            // Throw an exception or return null if the input format is invalid
            throw new IllegalArgumentException("Invalid input format: " + input);
            // return null; // Alternatively, you can return null if you prefer
        }
    }
    
    private ChangeDataTrackingPointer updateRehydrationToComplete(ChangeDataTrackingPointer changeDataTrackingPointer) {
        // Rehydration Completed
        System.out.println("Path to complete====>" + changeDataTrackingPointer.getFolderName());
        int rehydrationStatus = 2;
        changeDataTrackingPointer.setRehydrationStatus(rehydrationStatus);
        changeDataTrackingPointer = changeDataTrackingPointerService.save(changeDataTrackingPointer);
        return changeDataTrackingPointer;
    }

    private ChangeDataTracking updateStageStatusCDC(ChangeDataTracking changeDataTracking, int status) {
        // All records in hot tier
        System.out.println("Update CDC table:" + changeDataTracking.getCdcTableName() + " to " + status);
        changeDataTracking.setStageStatus(status);
        changeDataTracking = changeDataTrackingService.save(changeDataTracking);
        return changeDataTracking;
    }

    private ChangeDataTrackingPointer updateStageStatusToFail(ChangeDataTrackingPointer changeDataTrackingPointer) {
        // Rehydration Completed
        System.out.println("Path to fail====>" + changeDataTrackingPointer.getFolderName());

        int stageStatus = 2;
        changeDataTrackingPointer.setStageStatus(stageStatus);
        changeDataTrackingPointer = changeDataTrackingPointerService.save(changeDataTrackingPointer);
        return changeDataTrackingPointer;
    }

    private ChangeDataTrackingPointer updateStageStatusToComplete(ChangeDataTrackingPointer changeDataTrackingPointer) {
        // Rehydration Completed
        System.out.println("Path to stage complete====>" + changeDataTrackingPointer.getFolderName());

        int stageStatus = 1;
        changeDataTrackingPointer.setStageStatus(stageStatus);
        changeDataTrackingPointer = changeDataTrackingPointerService.save(changeDataTrackingPointer);
        return changeDataTrackingPointer;
    }

    private ChangeDataTrackingPointer updateRehydrationToStart(ChangeDataTrackingPointer changeDataTrackingPointer) {
        // Rehydration Started
        int rehydrationStatus = 1;
        changeDataTrackingPointer.setRehydrationStatus(rehydrationStatus);
        changeDataTrackingPointer = changeDataTrackingPointerService.save(changeDataTrackingPointer);
        return changeDataTrackingPointer;
    }

    @Scheduled(fixedRate = 60000) // 300000 milliseconds = 5 minutes
    public void quarantineRetry() {
        List<DatabaseConfig> databaseConfigs = databaseConfigService.getAllDatabaseConfigs();
        for (DatabaseConfig databaseConfig : databaseConfigs) {
            IntialLoad intialLoad=initialLoadService.getIntialLoad(databaseConfig.getDbIdentifier());
            if(intialLoad!=null){
                if(intialLoad.getQueueListenerStatus()==1){
                    if(pipelineService.countPipelineInProgress(databaseConfig.getDbIdentifier())==0){
                        errorHandle(databaseConfig.getDbIdentifier());
                    }
                }
            }
        }
    }

    private void errorHandle(String dbIdentifier) {
        List<MetaDataCatlog> metaDataCatlogs = metaDataCatlogService.findByQuarintineAndDbIdentifier(1, dbIdentifier);
        for (MetaDataCatlog failMetaDataCatlog : metaDataCatlogs) {
            List<HealthMetrics> nhealthMetrics = healthMetricsService
                    .findByfolderNameAndDbIdentifierAndTableNameAndStatus(failMetaDataCatlog.getLastUpdatedFolder(),
                            failMetaDataCatlog.getDbIdentifier(), failMetaDataCatlog.getTableName(), 2);
            for (HealthMetrics healthMetrics : nhealthMetrics) {
                bulkLoadErrorHandler.fixTruncateError(healthMetrics);
            }
            failMetaDataCatlog = updateErrorTableToStart(failMetaDataCatlog, dbIdentifier);
            unQuarintineTable(failMetaDataCatlog);
        }
    }

    private MetaDataCatlog unQuarintineTable(MetaDataCatlog metaDataCatlog) {
        int quarintine = 0;
        metaDataCatlog.setQuarintine(quarintine);
        metaDataCatlog = metaDataCatlogService.save(metaDataCatlog);
        return metaDataCatlog;
    }

    private MetaDataCatlog updateErrorTableToStart(MetaDataCatlog metaDataCatlog,
            String dbIdentifier) {
        MetaDataCatlog metaDataCatlog2 = null;
        FolderSyncStatus folderSyncStatus = folderSyncStatusService
                .getFolderSyncStatusOnFolderAndTableNameAndDBIdentifier(
                        metaDataCatlog.getLastUpdatedFolder(), metaDataCatlog.getTableName(), dbIdentifier);
        if (folderSyncStatus != null) {
            Short copyStatus = 0;
            folderSyncStatus.setCopyStatus(copyStatus);
            folderSyncStatusService.save(folderSyncStatus);
        }
        if (metaDataCatlog != null) {
            Short copyStatus = 1;
            metaDataCatlog.setLastCopyStatus(copyStatus);
            int retry = 0;
            metaDataCatlog.setRetry(retry);
            metaDataCatlog2 = metaDataCatlogService.save(metaDataCatlog);
        }
        return metaDataCatlog2;
    }

    @Scheduled(fixedRate = 60000) // 300000 milliseconds = 5 minutes
    public void purgeFiles() {
        List<DatabaseConfig> databaseConfigs = databaseConfigService.getAllDatabaseConfigs();
        for (DatabaseConfig databaseConfig : databaseConfigs) {
            long duration= databaseConfig.getPurgeDuration();
            LocalDateTime purgDateTime=calculateThresholdDate(duration);
            List<FolderSyncStatus> lstFolderSyncStatus=folderSyncStatusService.findFoldersAfterThreshold(databaseConfig.getDbIdentifier(),purgDateTime);
            for(FolderSyncStatus folderSyncStatus: lstFolderSyncStatus){
                String path = folderSyncStatus.getFolder() + "/" + folderSyncStatus.getTableName() + "/";
                System.out.println(path);
                System.out.println("Delete files before duration : "+ purgDateTime);
                boolean flag=purgeADLSFiles.deleteAllFilesInFolder(databaseConfig, databaseConfig.getAdlsContainerName(),path);
                if(flag){
                    folderSyncStatus.setDeleted(1);
                    folderSyncStatusService.save(folderSyncStatus);
                }
            }
        }
    }

    private LocalDateTime calculateThresholdDate(long durationInMilliseconds) {
        Instant now = Instant.now();
        Instant thresholdInstant = now.minusMillis(durationInMilliseconds);
        return LocalDateTime.ofInstant(thresholdInstant, ZoneId.systemDefault());
    }

}
