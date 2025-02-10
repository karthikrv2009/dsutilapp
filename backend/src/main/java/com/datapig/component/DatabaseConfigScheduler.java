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
import com.datapig.entity.MetaDataPointer;
import com.datapig.service.ArchivedFolderService;
import com.datapig.service.CDCLoaderService;
import com.datapig.service.ChangeDataTrackingCatalogService;
import com.datapig.service.ChangeDataTrackingPointerService;
import com.datapig.service.ChangeDataTrackingService;
import com.datapig.service.DatabaseConfigService;
import com.datapig.service.FolderSyncStatusService;
import com.datapig.service.MetaDataPointerService;
import com.datapig.utility.ArchiveToHotRehydration;

import java.time.LocalDateTime;
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
    private CDCLoaderService cdcLoaderService; 

    @Scheduled(fixedRate = 60000) // 300000 milliseconds = 5 minutes
    public void processDatabaseConfigs() {
        List<DatabaseConfig> configs = databaseConfigService.getAllDatabaseConfigs();
        for (DatabaseConfig config : configs) {
            if (config.isEnableArchive()) {
                List<MetaDataPointer> metaDataPointersNotArchived= archivedFolderService.listMetaDataPoicnterArchived(config.getDbIdentifier());
                for(MetaDataPointer metaDataPointer:metaDataPointersNotArchived){
                    createArchivedFolder(metaDataPointer);
                }
                List<ArchivedFolder> archivedFoldersNotArchived=archivedFolderService.findByStageStatusAndDbIdentifier(0, config.getDbIdentifier());
                for(ArchivedFolder archivedFolder:archivedFoldersNotArchived){
                    MetaDataPointer metaDataPointer=metaDataPointerService.getMetaDataPointerBydbIdentifierAndFolder(config.getDbIdentifier(), archivedFolder.getFolderName());
                    if(checkIfFolderArchivable(metaDataPointer,config.getDbIdentifier())){
                        boolean flag=moveBlobsToArchive(config,metaDataPointer);
                        if(flag){
                            updateArchivedFolder(archivedFolder);           
                        }       
                    }
                }
                List<ChangeDataTrackingPointer> changeDataTrackingPointersNotArchived = changeDataTrackingPointerService.findByDbIdentifierAndStageStatus(config.getDbIdentifier(), 2);
                for(ChangeDataTrackingPointer changeDataTrackingPointer:changeDataTrackingPointersNotArchived){
                    if(changeDataTrackingPointer.getFolderName().equalsIgnoreCase("/model.json")){
                        createCDCArchivedFolder(changeDataTrackingPointer);
                    }
                }
                List<ArchivedFolder> archivedCDCFoldersNotArchived=archivedFolderService.findByStageStatusAndDbIdentifier(0, config.getDbIdentifier());
                for(ArchivedFolder archivedFolder:archivedCDCFoldersNotArchived){
                        boolean flag=false;
                        boolean cdcFlag=true;
                        List<ChangeDataTrackingPointer>  changeDataTrackingPointers=changeDataTrackingPointerService.findByDbIdentifierAndFolderName(config.getDbIdentifier(), archivedFolder.getFolderName());
                        for(ChangeDataTrackingPointer changeDataTrackingPointer:changeDataTrackingPointers){
                            if(changeDataTrackingPointer.getStageStatus()!=2){
                                cdcFlag=false;        
                            }   
                        }
                        if(cdcFlag){
                            flag=moveCdcBlobsToArchive(config, archivedFolder);
                            if(flag){
                                updateArchivedFolder(archivedFolder);           
                            }
                        }
                }

            }
        }
    }

    private boolean checkIfFolderArchivable(MetaDataPointer metaDataPointer,String dbIdentifier){
        boolean flag=true;
        List<FolderSyncStatus> lst=folderSyncStatusService.getFolderSyncStatusByfolderAndDbIdentifier(metaDataPointer.getFolderName(), dbIdentifier);
        for(FolderSyncStatus folderSyncStatus:lst){
            if((folderSyncStatus.getCopyStatus()==2)||(folderSyncStatus.getCopyStatus()==0)){
                flag=false;
            }
        }
        return flag;
    } 


    private void createArchivedFolder(MetaDataPointer metaDataPointer){
        ArchivedFolder archivedFolder=new ArchivedFolder();
        if(metaDataPointer!=null){
            archivedFolder.setDbIdentifier(metaDataPointer.getDbIdentifier());
            archivedFolder.setFolderName(metaDataPointer.getFolderName());
            archivedFolder.setAdlsarchivetimestamp(LocalDateTime.now());
            archivedFolder.setStageStatus(0);
        }
        archivedFolderService.save(archivedFolder);
    }

    private void createCDCArchivedFolder(ChangeDataTrackingPointer changeDataTrackingPointer){
        ArchivedFolder archivedFolder=new ArchivedFolder();
        if(changeDataTrackingPointer!=null){
            archivedFolder.setDbIdentifier(changeDataTrackingPointer.getDbIdentifier());
            archivedFolder.setFolderName(changeDataTrackingPointer.getFolderName());
            archivedFolder.setAdlsarchivetimestamp(LocalDateTime.now());
            archivedFolder.setStageStatus(0);
        }
    }

    private void updateArchivedFolder(ArchivedFolder archivedFolder){
        if(archivedFolder!=null){
            archivedFolder.setAdlsarchivetimestamp(LocalDateTime.now());
            archivedFolder.setStageStatus(0);
        }
    }


    private boolean  moveBlobsToArchive(DatabaseConfig config,MetaDataPointer metaDataPointer) {
        boolean flag=false;
        String storageAccountUrl = config.getAdlsStorageAccountEndpoint();
        String sasToken = config.getAdlsStorageAccountSasKey();
        String containerName = config.getAdlsContainerName();
        String baseFolderPath = metaDataPointer.getFolderName();
        //metaDataPointerService.get
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

                flag=true;
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

    private boolean  moveCdcBlobsToArchive(DatabaseConfig config,ArchivedFolder archivedFolder) {
        boolean flag=false;
        String storageAccountUrl = config.getAdlsStorageAccountEndpoint();
        String sasToken = config.getAdlsStorageAccountSasKey();
        String containerName = config.getAdlsContainerName();
        String baseFolderPath = archivedFolder.getFolderName();
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
    
            flag=true;
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
        List<DatabaseConfig> databaseConfigs=databaseConfigService.getAllDatabaseConfigs();

        for(DatabaseConfig databaseConfig:databaseConfigs){
            int rehydrationInProgress=1;
            List<ChangeDataTracking> changeDataTrackings= changeDataTrackingService.findByStageStatusAndDbIdentifier(rehydrationInProgress, databaseConfig.getDbIdentifier());
            for(ChangeDataTracking changeDataTracking:changeDataTrackings){
                int  rehydrationStatus=0;
                List<ChangeDataTrackingPointer> pointers=changeDataTrackingPointerService.findByCdcTableNameAndDbIdentifierAndRehydrationStatus(changeDataTracking.getCdcTableName(), databaseConfig.getDbIdentifier(), rehydrationStatus);
                for(ChangeDataTrackingPointer changeDataTrackingPointer:pointers){
                    String path="/"+changeDataTrackingPointer.getFolderName()+"/"+changeDataTracking.getTableName();
                    if(changeDataTrackingPointer.getFolderName().contains("/model.json")){
                        boolean flag=archiveToHotRehydration.rehydrateBlobToHotTier(databaseConfig.getAdlsContainerName(), path,databaseConfig);
                        if(flag){
                            updateRehydrationToStart(changeDataTrackingPointer);
                        }
                    }
                    else{
                        boolean flag=archiveToHotRehydration.rehydrateToHotTier(databaseConfig.getAdlsContainerName(), path,databaseConfig);
                        if(flag){
                            updateRehydrationToStart(changeDataTrackingPointer);
                        }
                    }
                }
                rehydrationStatus=1;
                List<ChangeDataTrackingPointer> pointersStarted=changeDataTrackingPointerService.findByCdcTableNameAndDbIdentifierAndRehydrationStatus(changeDataTracking.getCdcTableName(), databaseConfig.getDbIdentifier(), rehydrationStatus);
                for(ChangeDataTrackingPointer changeDataTrackingPointer:pointersStarted){
                    String path="/"+changeDataTrackingPointer.getFolderName()+"/"+changeDataTracking.getTableName();
                    if(changeDataTrackingPointer.getFolderName().contains("/model.json")){
                        boolean flag=archiveToHotRehydration.checkRehydrationStatusForBlob(databaseConfig.getAdlsContainerName(), path,databaseConfig);
                        if(flag){
                            updateRehydrationToComplete(changeDataTrackingPointer);
                        }
                    }
                    else{
                        boolean flag=archiveToHotRehydration.checkRehydrationStatus(databaseConfig.getAdlsContainerName(), path,databaseConfig);
                        if(flag){
                            updateRehydrationToComplete(changeDataTrackingPointer);
                            updateStageStatusToComplete(changeDataTrackingPointer);
                        }
                    }
                }
                List<ChangeDataTrackingPointer> pointersPerCDCTable = changeDataTrackingPointerService.findByCdcTableNameAndDbIdentifier(changeDataTracking.getCdcTableName(), databaseConfig.getDbIdentifier());
                boolean rehydrateState=true;
                for(ChangeDataTrackingPointer changeDataTrackingPointer:pointersPerCDCTable){
                    if(changeDataTrackingPointer.getRehydrationStatus()!=2){
                        rehydrateState=false;
                    }
                }
                if(rehydrateState){
                    updateRehydrationCDCToComplete(changeDataTracking);
                }
            }
            int readyToProcess=2;
            List<ChangeDataTracking> changeDataTrackings2=changeDataTrackingService.findByStageStatusAndDbIdentifier(readyToProcess, databaseConfig.getDbIdentifier());
            for(ChangeDataTracking changeDataTracking:changeDataTrackings2){
                boolean masterFlag=true;
                ChangeDataTrackingCatalog changeDataTrackingCatalog=changeDataTrackingCatalogService.findbyCdcTableNameAndDbIdentifier(changeDataTracking.getCdcTableName(), changeDataTracking.getDbIdentifier());
                if(changeDataTrackingCatalog!=null){
                    List<ChangeDataTrackingPointer> pointers=changeDataTrackingPointerService.findByCdcTableNameAndDbIdentifier(changeDataTracking.getCdcTableName(), databaseConfig.getDbIdentifier());
                    for(ChangeDataTrackingPointer changeDataTrackingPointer:pointers){
                        if(!changeDataTrackingPointer.getFolderName().equalsIgnoreCase("/model.json")){
                            boolean flag=false;
                            flag=cdcLoaderService.stageDataFromADLS(databaseConfig.getAdlsStorageAccountName(), changeDataTrackingPointer.getFolderName(), changeDataTrackingPointer.getCdcTableName(), changeDataTrackingCatalog.getDataFrame(), changeDataTrackingCatalog.getSelectColumn());
                            if(flag){
                                updateStageStatusToComplete(changeDataTrackingPointer);
                            } 
                            else{
                                masterFlag=false;
                                updateStageStatusToFail(changeDataTrackingPointer);
                            }                           
                        
                        }

                    }
                    
                }
            if(masterFlag){
                //success
                updateStageStatusCDCToComplete(changeDataTracking,3);
            }
            else{
                updateStageStatusCDCToComplete(changeDataTracking,4);
            }
            }
        }
    }

    private ChangeDataTracking updateStageStatusCDCToComplete(ChangeDataTracking changeDataTracking,int status){
        //All records in hot tier
        changeDataTracking.setStageStatus(status);
        changeDataTracking= changeDataTrackingService.save(changeDataTracking);
        return changeDataTracking;
    }
    
    private ChangeDataTracking updateRehydrationCDCToComplete(ChangeDataTracking changeDataTracking){
        //All records in hot tier
        int rehydrationStatus = 2;
        changeDataTracking.setStageStatus(rehydrationStatus);
        changeDataTracking= changeDataTrackingService.save(changeDataTracking);
        return changeDataTracking;
    }

    private void updateStageStatusToComplete(ChangeDataTrackingPointer changeDataTrackingPointer){
        //Rehydration Completed
        int stageStatus = 1;
        changeDataTrackingPointer.setRehydrationStatus(stageStatus);
        changeDataTrackingPointerService.save(changeDataTrackingPointer);
    }

    private void updateStageStatusToFail(ChangeDataTrackingPointer changeDataTrackingPointer){
        //Rehydration Completed
        int stageStatus = 2;
        changeDataTrackingPointer.setRehydrationStatus(stageStatus);
        changeDataTrackingPointerService.save(changeDataTrackingPointer);
    }

    private void updateRehydrationToComplete(ChangeDataTrackingPointer changeDataTrackingPointer){
        //Rehydration Completed
        int rehydrationStatus = 2;
        changeDataTrackingPointer.setRehydrationStatus(rehydrationStatus);
        changeDataTrackingPointerService.save(changeDataTrackingPointer);
    }

    private void updateRehydrationToStart(ChangeDataTrackingPointer changeDataTrackingPointer){
        //Rehydration Started
        int rehydrationStatus = 1;
        changeDataTrackingPointer.setRehydrationStatus(rehydrationStatus);
        changeDataTrackingPointerService.save(changeDataTrackingPointer);
    }

}
