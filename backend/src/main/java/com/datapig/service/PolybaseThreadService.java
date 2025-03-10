package com.datapig.service;

import org.springframework.jdbc.core.JdbcTemplate;

import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.HealthMetrics;
import com.datapig.entity.MetaDataCatlog;
import com.datapig.entity.MetaDataPointer;
import com.datapig.entity.Pipeline;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolybaseThreadService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PolybaseThreadService.class);

    private final MetaDataCatlog metaDataCatlog;
    private final FolderSyncStatus folderSyncStatus;
    private final MetaDataPointer metaDataPointer;
    private final Pipeline pipeline;
    private final JdbcTemplate jdbcTemplate;
    private final MetaDataCatlogService metaDataCatlogService;
    private final FolderSyncStatusService folderSyncStatusService;
    private final HealthMetricsService healthMetricsService;


    private long timespent = 0;
    private int status = 0;

    public PolybaseThreadService(MetaDataCatlog metaDataCatlog, FolderSyncStatus folderSyncStatus,
            MetaDataPointer metaDataPointer, Pipeline pipeline, JdbcTemplate jdbcTemplate,
            MetaDataCatlogService metaDataCatlogService, FolderSyncStatusService folderSyncStatusService,
            HealthMetricsService healthMetricsService) {
        // Your processing logic here, using tableName and folder
        this.metaDataCatlog = metaDataCatlog;
        this.folderSyncStatus = folderSyncStatus;
        this.metaDataPointer = metaDataPointer;
        this.pipeline = pipeline;
        this.jdbcTemplate = jdbcTemplate;
        this.folderSyncStatusService = folderSyncStatusService;
        this.metaDataCatlogService = metaDataCatlogService;
        this.healthMetricsService = healthMetricsService;
    }



    @Override
    public void run() {
        HealthMetrics healthMetrics = null;
        String tableName = folderSyncStatus.getTableName();
        String folder = folderSyncStatus.getFolder();
        String dataFrame = metaDataCatlog.getDataFrame();
        String selectColumn = metaDataCatlog.getSelectColumn();
        String columnNames = metaDataCatlog.getColumnNames();
        String selectDataFrame=metaDataCatlog.getSelectDataFrame();
        String data_source = metaDataPointer.getStorageAccount();
        boolean flag = true;
        int errorFlag = 0;

        while (flag) {
            createStagingTable(tableName, dataFrame);
            errorFlag = errorFlag + 1;
            healthMetrics = stageDataFromADLS(data_source, folder, tableName, dataFrame, selectColumn,selectDataFrame);


            if (healthMetrics != null) {
                if ((healthMetrics.getStatus() == 1)
                        && (healthMetrics.getMethodname().equalsIgnoreCase("StageDataFromADLS"))) {
                    healthMetrics = cleanupTargetTable(tableName);
                }
            }
            if (healthMetrics != null) {
                if ((healthMetrics.getStatus() == 1) && (healthMetrics.getMethodname()
                        .equalsIgnoreCase("DeleteRecordsOnTargetTableBasedOnChangeFeed"))) {
                    healthMetrics = cleanupSourceTable(tableName);
                }
            }

            if (healthMetrics != null) {
                if ((healthMetrics.getStatus() == 1)
                        && (healthMetrics.getMethodname().equalsIgnoreCase("DeleteRecordsOnSourceTableBasedOnChangeFeed"))) {
                    healthMetrics = cleanupSourceTableForLatest(tableName);
                }
            }

            if (healthMetrics != null) {
                if ((healthMetrics.getStatus() == 1) && (healthMetrics.getMethodname()
                        .equalsIgnoreCase("CleanUpStageData"))) {
                    healthMetrics = createMergeSql(tableName, columnNames);
                }
            }

            if (healthMetrics != null) {
                if ((healthMetrics.getStatus() == 1)
                        && (healthMetrics.getMethodname().equalsIgnoreCase("DedupAndMergeFromSourceToTarget"))) {
                    postMergeAction(metaDataCatlog, folderSyncStatus);
                    flag = false;
                } else {
                    if (errorFlag > 2) {
                        postMergeActionOnFail(metaDataCatlog, folderSyncStatus);
                        flag = false;
                    }
                }
                errorFlag = errorFlag + 1;
            }

        }

    }

    private HealthMetrics logHealthMetric(Pipeline pipeline, FolderSyncStatus folderSyncStatus, String methodAction,
            long timeTaken, int status, long rowCount,String errorMsg) {
        HealthMetrics healthMetrics = new HealthMetrics();
        healthMetrics.setPipelineId(pipeline.getPipelineid());
        healthMetrics.setFolderName(folderSyncStatus.getFolder());
        healthMetrics.setTableName(folderSyncStatus.getTableName());
        healthMetrics.setMethodname(methodAction);
        healthMetrics.setTimespent(timeTaken);
        healthMetrics.setRcount(rowCount);
        healthMetrics.setStatus(status);
        healthMetrics.setDbIdentifier(folderSyncStatus.getDbIdentifier());
        healthMetrics.setErrorMsg(errorMsg);
        return healthMetricsService.save(healthMetrics);
    }

    private HealthMetrics stageDataFromADLS(String dataSource, String folder, String tableName, String dataFrame,
            String selectColumn,String selectDataFrame) {
        HealthMetrics healthMetrics = null;
        int rowcount = 0;
        long startTime = System.currentTimeMillis();
        String query = "INSERT INTO dbo._staging_" + tableName +
                " SELECT " + selectColumn +
                " FROM OPENROWSET(BULK '/" + folder + "/" + tableName + "/*.csv', FORMAT = 'CSV', DATA_SOURCE = '"
                + dataSource + "',CODEPAGE='65001') " +
                "WITH (" + selectDataFrame + ") AS " + tableName;
        logger.info(query);
        try {
            rowcount = jdbcTemplate.update(query);
            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 1;
            String methodAction = "StageDataFromADLS";
            String errorMsg="";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);
        } catch (Exception e) {

            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 2;
            String methodAction = "StageDataFromADLS";
            String errorMsg= getMainCauseMessage(e,query);
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);
            logger.warn("Execution failed: " + e.getMessage());
        }

        return healthMetrics;
    }

    private HealthMetrics createMergeSql(String tableName, String columnNames) {
        HealthMetrics healthMetrics = null;
        int rowcount = 0;
        long startTime = System.currentTimeMillis();

        String[] columns = columnNames.split(",");
        StringBuilder updateStatements = new StringBuilder();
        StringBuilder valuesColumns = new StringBuilder();

        for (String col : columns) {
            updateStatements.append("target.").append(col).append(" = source.").append(col).append(",");
            valuesColumns.append("source.").append(col).append(",");
        }

        updateStatements.deleteCharAt(updateStatements.length() - 1);
        valuesColumns.deleteCharAt(valuesColumns.length() - 1);

        String mergeQuery = "DECLARE @mergeResults TABLE (Action NVARCHAR(20)); " +
                "MERGE INTO dbo." + tableName + " AS target " +
                "USING dbo._staging_" + tableName + " AS source " +
                "ON target.Id = source.Id " +
                "WHEN MATCHED AND (target.versionnumber < source.versionnumber) THEN " +
                "UPDATE SET " + updateStatements.toString() +
                " WHEN NOT MATCHED BY TARGET THEN " +
                "INSERT (" + columnNames + ") " +
                "VALUES (" + valuesColumns.toString() + ") " +
                "OUTPUT $action INTO @mergeResults; " +
                "SELECT COUNT(*) FROM @mergeResults;";

        logger.debug("Executed SQL: " + mergeQuery);
        try {
            Integer rows = jdbcTemplate.queryForObject(mergeQuery, Integer.class);
            rowcount = (rows != null) ? rows : 0;
            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 1;
            String methodAction = "DedupAndMergeFromSourceToTarget";
            String errorMsg="";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);
        } catch (Exception e) {

            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 2;
            String methodAction = "DedupAndMergeFromSourceToTarget";
            String errorMsg= getMainCauseMessage(e,mergeQuery);
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);            logger.warn("Execution failed: " + e.getMessage());
        }
        return healthMetrics;
    }

    private HealthMetrics cleanupSourceTableForLatest(String tableName) {
        HealthMetrics healthMetrics = null;
        int rowcount = 0;
        long startTime = System.currentTimeMillis();

        String query = "DELETE t \r\n" + 
                        "FROM dbo._staging_"+tableName+" t JOIN\r\n" + 
                        "(SELECT id,SinkModifiedon,versionnumber,\r\n" + 
                        "ROW_NUMBER() over (PARTITION BY id ORDER BY SinkModifiedon DESC,versionnumber DESC) AS RowNum\r\n" + 
                        "FROM dbo._staging_"+tableName+" WHERE IsDelete <> 1 ) as s\r\n" + 
                        "ON s.Id=t.Id\r\n" + 
                        "AND s.versionnumber=t.versionnumber\r\n" + 
                        "AND s.SinkModifiedOn=t.SinkModifiedOn\r\n" + 
                        "AND s.RowNum>1";

        try {
            rowcount = jdbcTemplate.update(query);
            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 1;
            String methodAction = "CleanUpStageData";
            String errorMsg="";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);
        } catch (Exception e) {

            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 2;
            String methodAction = "CleanUpStageData";
            String errorMsg= getMainCauseMessage(e,query);
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);
            logger.warn("Execution failed: " + e.getMessage());
        }
        return healthMetrics;
    }

    private HealthMetrics cleanupTargetTable(String tableName) {
        HealthMetrics healthMetrics = null;
        int rowcount = 0;
        long startTime = System.currentTimeMillis();

        String query = "DELETE target\n" + //
                "FROM " + tableName + " target\n" + //
                "INNER JOIN _staging_" + tableName + " source \n" + //
                "  ON source.id = target.id\n" + //
                "WHERE source.IsDelete = 1;";

        try {
            rowcount = jdbcTemplate.update(query);
            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 1;
            String methodAction = "DeleteRecordsOnTargetTableBasedOnChangeFeed";
            String errorMsg="";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);
        } catch (Exception e) {

            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 2;
            String methodAction = "DeleteRecordsOnTargetTableBasedOnChangeFeed";
            String errorMsg= getMainCauseMessage(e,query);
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);
            logger.warn("Execution failed: " + e.getMessage());
        }
        return healthMetrics;
    }

    private HealthMetrics cleanupSourceTable(String tableName) {
        tableName="dbo._staging_"+tableName;
        HealthMetrics healthMetrics = null;
        int rowcount = 0;
        long startTime = System.currentTimeMillis();

        String query = "DELETE \n" + //
                "FROM " + tableName + "\n" + //
                "WHERE IsDelete = 1;";

        try {
            rowcount = jdbcTemplate.update(query);
            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 1;
            String methodAction = "DeleteRecordsOnSourceTableBasedOnChangeFeed";
            String errorMsg="";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);
        } catch (Exception e) {

            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 2;
            String methodAction = "DeleteRecordsOnSourceTableBasedOnChangeFeed";
            String errorMsg= getMainCauseMessage(e,query);
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount,errorMsg);
            logger.warn("Execution failed: " + e.getMessage());
        }
        return healthMetrics;
    }


    private void createStagingTable(String tableName, String dataFrame) {
        String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_"
                + tableName + "') " +
                "DROP TABLE dbo._staging_" + tableName;
        String createTableSQL = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_"
                + tableName + "') " +
                "CREATE TABLE dbo._staging_" + tableName + "(" + dataFrame + ")";

        jdbcTemplate.execute(dropTableSQL);
        jdbcTemplate.execute(createTableSQL);
    }

    private void postMergeAction(MetaDataCatlog metaDataCatlog, FolderSyncStatus folderSyncStatus) {
        String tableName = metaDataCatlog.getTableName();
        String dropTableSQL = "DROP TABLE IF EXISTS dbo._staging_" + tableName;
        jdbcTemplate.execute(dropTableSQL);

        Short metaCopyStatus = 2;
        int retry = 0;
        metaDataCatlog.setLastCopyStatus(metaCopyStatus);
        metaDataCatlog.setLastEndCopyDate(LocalDateTime.now());
        metaDataCatlog.setLastUpdatedFolder(folderSyncStatus.getFolder());
        metaDataCatlog.setRetry(retry);
        metaDataCatlogService.save(metaDataCatlog);

        Short copyStatus = 1;
        folderSyncStatus.setCopyStatus(copyStatus);
        folderSyncStatusService.save(folderSyncStatus);

    }

    private void postMergeActionOnFail(MetaDataCatlog metaDataCatlog, FolderSyncStatus folderSyncStatus) {
        String tableName = metaDataCatlog.getTableName();

        String dropTableSQL = "DROP TABLE IF EXISTS dbo._staging_" + tableName;
        jdbcTemplate.execute(dropTableSQL);

        Short metaCopyStatus = 3;
        metaDataCatlog.setLastCopyStatus(metaCopyStatus);
        metaDataCatlog.setLastEndCopyDate(LocalDateTime.now());
        metaDataCatlog.setLastUpdatedFolder(folderSyncStatus.getFolder());
        metaDataCatlogService.save(metaDataCatlog);
        Short copyStatus = 2;
        folderSyncStatus.setCopyStatus(copyStatus);
        folderSyncStatusService.save(folderSyncStatus);
    }

    public static String getMainCauseMessage(Throwable e, String query) {
        // Navigate to the root cause
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
    
        // Combine the cause and the query
        return cause.getMessage() + "\n\nQuery: " + query;
    }
    
}
