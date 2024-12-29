package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.datapig.component.DynamicDataSourceManager;
import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.HealthMetrics;
import com.datapig.entity.MetaDataCatlog;
import com.datapig.entity.MetaDataPointer;
import com.datapig.entity.Pipeline;

import java.time.LocalDateTime;

import javax.sql.DataSource;

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

    @Autowired
    private DynamicDataSourceManager dynamicDataSourceManager;

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
        this.jdbcTemplate = getJdbcTemplate(metaDataPointer.getDbIdentifier());
        this.folderSyncStatusService = folderSyncStatusService;
        this.metaDataCatlogService = metaDataCatlogService;
        this.healthMetricsService = healthMetricsService;
    }

    public JdbcTemplate getJdbcTemplate(String dbIdentifier) {
        // Get the DataSource from DynamicDataSourceManager
        DataSource dataSource = dynamicDataSourceManager.getDataSource(dbIdentifier);
        // Create and return a new JdbcTemplate based on the DataSource
        return new JdbcTemplate(dataSource);
    }

    @Override
    public void run() {
        HealthMetrics healthMetrics = null;
        String tableName = folderSyncStatus.getTableName();
        String folder = folderSyncStatus.getFolder();
        String dataFrame = metaDataCatlog.getDataFrame();
        String selectColumn = metaDataCatlog.getSelectColumn();
        String columnNames = metaDataCatlog.getColumnNames();
        String dbIdentifier = metaDataPointer.getDbIdentifier();
        String data_source = metaDataPointer.getStorageAccount();
        boolean flag = true;
        int errorFlag = 0;

        while (flag) {
            createStagingTable(tableName, dataFrame);
            errorFlag = errorFlag + 1;
            healthMetrics = stageDataFromADLS(data_source, folder, tableName, dataFrame, selectColumn);
            if (healthMetrics != null) {
                if ((healthMetrics.getStatus() == 1)
                        && (healthMetrics.getMethodname().equalsIgnoreCase("StageDataFromADLS"))) {
                    healthMetrics = cleanupSourceTableForLatest(tableName);
                }
            }

            if (healthMetrics != null) {
                if ((healthMetrics.getStatus() == 1)
                        && (healthMetrics.getMethodname().equalsIgnoreCase("CleanUpStageData"))) {
                    healthMetrics = cleanupTargetTable(tableName);
                }
            }

            if (healthMetrics != null) {
                if ((healthMetrics.getStatus() == 1) && (healthMetrics.getMethodname()
                        .equalsIgnoreCase("DeleteRecordsOnTargetTableBasedOnChangeFeed"))) {
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
            long timeTaken, int status, long rowCount) {
        HealthMetrics healthMetrics = new HealthMetrics();
        healthMetrics.setPipelineId(pipeline.getPipelineid());
        healthMetrics.setFolderName(folderSyncStatus.getFolder());
        healthMetrics.setTableName(folderSyncStatus.getTableName());
        healthMetrics.setMethodname(methodAction);
        healthMetrics.setTimespent(timeTaken);
        healthMetrics.setRcount(rowCount);
        healthMetrics.setStatus(status);
        return healthMetricsService.save(healthMetrics);
    }

    private HealthMetrics stageDataFromADLS(String dataSource, String folder, String tableName, String dataFrame,
            String selectColumn) {
        HealthMetrics healthMetrics = null;
        int rowcount = 0;
        long startTime = System.currentTimeMillis();
        String query = "INSERT INTO dbo._staging_" + tableName +
                " SELECT " + selectColumn +
                " FROM OPENROWSET(BULK '/" + folder + "/" + tableName + "/*.csv', FORMAT = 'CSV', DATA_SOURCE = '"
                + dataSource + "') " +
                "WITH (" + dataFrame + ") AS " + tableName;
        logger.info(query);
        try {
            rowcount = jdbcTemplate.update(query);
            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 1;
            String methodAction = "StageDataFromADLS";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount);
        } catch (Exception e) {

            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 2;
            String methodAction = "StageDataFromADLS";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount);
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
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount);

        } catch (Exception e) {

            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 2;
            String methodAction = "DedupAndMergeFromSourceToTarget";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount);
            logger.warn("Execution failed: " + e.getMessage());
        }
        return healthMetrics;
    }

    private HealthMetrics cleanupSourceTableForLatest(String tableName) {
        HealthMetrics healthMetrics = null;
        int rowcount = 0;
        long startTime = System.currentTimeMillis();

        String query = "DELETE FROM _staging_" + tableName + "\r\n" + //
                "WHERE NOT EXISTS (\r\n" + //
                "    SELECT 1\r\n" + //
                "    FROM (\r\n" + //
                "        SELECT id, versionnumber, MAX(sinkmodifiedon) AS latest_modifieddate\r\n" + //
                "        FROM _staging_" + tableName + "\r\n" + //
                "        GROUP BY id, versionnumber\r\n" + //
                "    ) subquery\r\n" + //
                "    WHERE subquery.id = _staging_" + tableName + ".id\r\n" + //
                "      AND subquery.versionnumber = _staging_" + tableName + ".versionnumber\r\n" + //
                "      AND subquery.latest_modifieddate = _staging_" + tableName + ".sinkmodifiedon\r\n" + //
                ") AND  _staging_" + tableName + ".IsDelete NOT in ('1','True')\r\n" + //
                "";

        try {
            rowcount = jdbcTemplate.update(query);
            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 1;
            String methodAction = "CleanUpStageData";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount);
        } catch (Exception e) {

            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 2;
            String methodAction = "CleanUpStageData";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount);
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
                "  ON source.recid = target.recid\n" + //
                "WHERE source.IsDelete IN ('1', 'True');";

        try {
            rowcount = jdbcTemplate.update(query);
            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 1;
            String methodAction = "DeleteRecordsOnTargetTableBasedOnChangeFeed";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount);
        } catch (Exception e) {

            long endTime = System.currentTimeMillis();
            timespent = endTime - startTime;
            status = 2;
            String methodAction = "DeleteRecordsOnTargetTableBasedOnChangeFeed";
            healthMetrics = logHealthMetric(pipeline, folderSyncStatus, methodAction, timespent, status, rowcount);
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

}
