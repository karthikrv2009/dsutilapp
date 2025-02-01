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
import com.azure.storage.blob.models.BlobStorageException;
import com.datapig.entity.ArchivedFolder;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.MetaDataPointer;
import com.datapig.service.ArchivedFolderService;
import com.datapig.service.DatabaseConfigService;
import com.datapig.service.FolderSyncStatusService;
import com.datapig.service.MetaDataPointerService;

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

    @Scheduled(fixedRate = 300000) // 300000 milliseconds = 5 minutes
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

                // Set the access tier to Archive
                blobClient.setAccessTier(AccessTier.ARCHIVE);
                System.out.println("Blob '" + blobName + "' moved to Archive tier successfully.");
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

}
