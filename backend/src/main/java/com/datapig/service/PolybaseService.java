package com.datapig.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.datapig.component.EncryptedPropertyReader;
import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.HealthMetrics;
import com.datapig.entity.MetaDataCatlog;
import com.datapig.entity.MetaDataPointer;
import com.datapig.entity.Pipeline;
import com.datapig.utility.JDBCTemplateUtiltiy;

import java.time.LocalDateTime;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import org.slf4j.Logger;

@Service
public class PolybaseService {

    private static final Logger logger = LoggerFactory.getLogger(PolybaseService.class);

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

    @Autowired
    private HealthMetricsService healthMetricsService;

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private EncryptedPropertyReader encryptedPropertyReader;

    public void startSyncInFolder(MetaDataPointer metaDataPointer) {

        int maxCount = Integer.parseInt(encryptedPropertyReader.getProperty("MAX_THREAD_COUNT"));
        List<FolderSyncStatus> setfolderSyncStatus = folderSyncStatusService
                .getFolderSyncStatusByfolderAndDbIdentifier(metaDataPointer.getFolderName(),
                        metaDataPointer.getDbIdentifier());

        List<FolderSyncStatus> folderNeedsToBeProcessed = new ArrayList<FolderSyncStatus>();
        for (FolderSyncStatus folderSyncStatus1 : setfolderSyncStatus) {
            MetaDataCatlog metaDataCatlog = metaDataCatlogService
                    .getMetaDataCatlogByTableNameAndDbIdentifier(folderSyncStatus1.getTableName(),
                            folderSyncStatus1.getDbIdentifier());
            if ((metaDataCatlog.getLastCopyStatus() != 3) && (metaDataCatlog.getQuarintine() != 1)) {
                if (folderSyncStatus1.getCopyStatus() == 0) {
                    folderNeedsToBeProcessed.add(folderSyncStatus1);
                }
            }
        }

        List<List<FolderSyncStatus>> chunksofFolderSyncStatus = chunkList(folderNeedsToBeProcessed, maxCount);

        for (List<FolderSyncStatus> chunk : chunksofFolderSyncStatus) {

            Pipeline pipeline = createNewPipeline(metaDataPointer.getFolderName(), metaDataPointer.getDbIdentifier());
            // Create an ExecutorService with a fixed thread pool
            ExecutorService executorService = Executors.newFixedThreadPool(chunk.size());
            for (FolderSyncStatus folderSyncStatus : chunk) {
                MetaDataCatlog metaDataCatlog = preMergeActionFolderSyncStatus(folderSyncStatus);
                if (metaDataCatlog != null) {
                    PolybaseThreadService polybaseThreadService = new PolybaseThreadService(metaDataCatlog,
                            folderSyncStatus, metaDataPointer, pipeline, jdbcTemplate, metaDataCatlogService,
                            folderSyncStatusService, healthMetricsService);
                    executorService.submit(polybaseThreadService);
                }
            }
            // Shutdown the executor service
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                    logger.warn("Tasks did not finish in the allotted time, forcing shutdown.");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error("Thread was interrupted while waiting for tasks to complete.");
                executorService.shutdownNow();
            }
            postMergeActionPipeline(pipeline);
        }
        postMergeActionMetaDataPointer(metaDataPointer);
        logger.info("All data merge tasks completed for folder: " + metaDataPointer.getFolderName());

    }

    private void postMergeActionPipeline(Pipeline pipeline) {
        pipeline.setPipelineEndTime(LocalDateTime.now());
        List<HealthMetrics> healthMetrics = healthMetricsService.findbyPipelineId(pipeline.getPipelineid());
        boolean flag = true;
        for (HealthMetrics healtmet : healthMetrics) {
            if (healtmet.getStatus() == 2) {
                flag = false;
            }
        }
        if (flag) {
            pipeline.setStatus(2);
        } else {
            pipeline.setStatus(3);
        }
        pipelineService.save(pipeline);

    }

    private List<List<FolderSyncStatus>> chunkList(List<FolderSyncStatus> inputList, int maxCount) {
        List<List<FolderSyncStatus>> chunks = new ArrayList<>();

        int totalSize = inputList.size();
        int numChunks = (totalSize / maxCount) + (totalSize % maxCount == 0 ? 0 : 1);

        logger.info("Total size of list: " + totalSize);
        logger.info("Max items per chunk: " + maxCount);
        logger.info("Number of chunks to create: " + numChunks);

        for (int i = 0; i < numChunks; i++) {
            int start = i * maxCount;
            int end = Math.min(start + maxCount, totalSize);

            logger.info("Creating chunk from index " + start + " to " + (end - 1));
            List<FolderSyncStatus> chunk = inputList.subList(start, end);
            chunks.add(chunk);
            logger.info("Chunk " + (i + 1) + ": " + chunk);
        }

        return chunks;
    }

    private Pipeline createNewPipeline(String foldername, String dbIdentifier) {
        Pipeline pipeline = new Pipeline();
        pipeline.setPipelineid(java.util.UUID.randomUUID().toString());
        pipeline.setFolderName(foldername);
        pipeline.setStatus(1);
        pipeline.setPipelineStartTime(LocalDateTime.now());
        pipeline.setDbIdentifier(dbIdentifier);
        pipeline = pipelineService.save(pipeline);
        return pipeline;
    }

    private void postMergeActionMetaDataPointer(MetaDataPointer metaDataPointer) {
        Short copyStatus = 2;
        metaDataPointer.setStageStatus(copyStatus);
        metaDataPointer.setStageEndTime(LocalDateTime.now());
        metaDataPointerService.save(metaDataPointer);
    }

    private MetaDataCatlog preMergeActionFolderSyncStatus(FolderSyncStatus folderSyncStatus) {
        jdbcTemplateUtiltiy.dropStagingTable(folderSyncStatus.getTableName(), folderSyncStatus.getDbIdentifier());
        Short copyStatus = 2;
        String tableName = folderSyncStatus.getTableName();
        MetaDataCatlog metaDataCatlog = metaDataCatlogService.getmetaDataCatlogServiceBytableName(tableName);
        metaDataCatlog.setLastCopyStatus(copyStatus);
        metaDataCatlog.setLastStartCopyDate(LocalDateTime.now());
        metaDataCatlog.setLastUpdatedFolder(folderSyncStatus.getFolder());
        metaDataCatlog = metaDataCatlogService.save(metaDataCatlog);
        return metaDataCatlog;
    }

}
