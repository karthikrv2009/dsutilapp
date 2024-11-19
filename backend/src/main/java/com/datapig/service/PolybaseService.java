package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.MetaDataCatlog;
import com.datapig.entity.MetaDataPointer;
import com.datapig.utility.JDBCTemplateUtiltiy;

import java.time.LocalDateTime;
import java.util.List; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

@Service
public class PolybaseService {

    @Autowired
    private JDBCTemplateUtiltiy jdbcTemplateUtiltiy;

    @Autowired
    private MetaDataPointerService metaDataPointerService;

    @Autowired
    MetaDataCatlogService metaDataCatlogService;

    @Autowired
    private FolderSyncStatusService folderSyncStatusService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
/* 
    // Replace the old instantiation like this:
    public void startPolybaseProcess(MetaDataCatlog metaDataCatlog, FolderSyncStatus folderSyncStatus,MetaDataPointer metaDataPointer) {
    // Assuming your PolybaseThreadService requires certain parameters
    polybaseThreadService.runTask(metaDataCatlog, folderSyncStatus,metaDataPointer);
    }
*/
    public void startSyncInFolder(MetaDataPointer metaDataPointer) {

        List<FolderSyncStatus> setfolderSyncStatus = folderSyncStatusService.getFolderSyncStatusByfolder(metaDataPointer.getFolderName());

        List<FolderSyncStatus> folderNeedsToBeProcessed= new ArrayList<FolderSyncStatus>();
        for(FolderSyncStatus folderSyncStatus1:setfolderSyncStatus){
            if(folderSyncStatus1.getCopyStatus()==0){
                folderNeedsToBeProcessed.add(folderSyncStatus1);
            }
        }
        // Create an ExecutorService with a fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(folderNeedsToBeProcessed.size());

        for (FolderSyncStatus folderSyncStatus : folderNeedsToBeProcessed) {
            MetaDataCatlog metaDataCatlog=preMergeAction(folderSyncStatus);
            PolybaseThreadService polybaseThreadService= new PolybaseThreadService(metaDataCatlog, folderSyncStatus, metaDataPointer,jdbcTemplate,metaDataCatlogService,folderSyncStatusService);
            executorService.submit(polybaseThreadService);
        }

        // Shutdown the executor service
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                System.out.println("Tasks did not finish in the allotted time, forcing shutdown.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted while waiting for tasks to complete.");
            executorService.shutdownNow();
        }
        postMergeAction(metaDataPointer);
        System.out.println("All data merge tasks completed for folder: " + metaDataPointer.getFolderName());
    }

    private void postMergeAction(MetaDataPointer metaDataPointer){
        Short copyStatus=2;
        metaDataPointer.setStageStatus(copyStatus);
        metaDataPointer.setStageTime(LocalDateTime.now());
        metaDataPointerService.save(metaDataPointer);
    }

    private MetaDataCatlog preMergeAction(FolderSyncStatus folderSyncStatus) {
        jdbcTemplateUtiltiy.dropStagingTable(folderSyncStatus.getTableName());
        Short copyStatus=2;
        String tableName=folderSyncStatus.getTableName();
        MetaDataCatlog metaDataCatlog= metaDataCatlogService.getmetaDataCatlogServiceBytableName(tableName);
        metaDataCatlog.setLastCopyStatus(copyStatus);
        metaDataCatlog.setLastStartCopyDate(LocalDateTime.now());
        metaDataCatlog.setLastUpdatedFolder(folderSyncStatus.getFolder());
        metaDataCatlog=metaDataCatlogService.save(metaDataCatlog);
        return metaDataCatlog;
   }

}

