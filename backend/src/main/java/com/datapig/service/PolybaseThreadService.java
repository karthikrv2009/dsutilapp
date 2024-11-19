package com.datapig.service;

import org.springframework.jdbc.core.JdbcTemplate;

import com.datapig.entity.FolderSyncStatus;
import com.datapig.entity.MetaDataCatlog;
import com.datapig.entity.MetaDataPointer;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class PolybaseThreadService implements Runnable {

    private final MetaDataCatlog metaDataCatlog;
    private  final FolderSyncStatus folderSyncStatus;
    private  final MetaDataPointer metaDataPointer;
    private  final JdbcTemplate jdbcTemplate;
    private  final MetaDataCatlogService metaDataCatlogService;
    private  final FolderSyncStatusService folderSyncStatusService;


     public  PolybaseThreadService(MetaDataCatlog metaDataCatlog, FolderSyncStatus folderSyncStatus,MetaDataPointer metaDataPointer,JdbcTemplate jdbcTemplate,MetaDataCatlogService metaDataCatlogService,FolderSyncStatusService folderSyncStatusService) {
        // Your processing logic here, using tableName and folder
        this.metaDataCatlog=metaDataCatlog;
        this.folderSyncStatus=folderSyncStatus;
        this.metaDataPointer=metaDataPointer;
        this.jdbcTemplate=jdbcTemplate;
        this.folderSyncStatusService=folderSyncStatusService;
        this.metaDataCatlogService=metaDataCatlogService;
        //run(); // or other methods to process
    }

    @Override
    public void run() {
        String tableName=folderSyncStatus.getTableName();
        String folder=folderSyncStatus.getFolder();
        String dataFrame = metaDataCatlog.getDataFrame();
        String selectColumn = metaDataCatlog.getSelectColumn();
        String columnNames = metaDataCatlog.getColumnNames();
        String data_source=metaDataPointer.getStorageAccount();

        int errorFlag = 0;
        createStagingTable(tableName, dataFrame);
        
        try {
            stageDataFromADLS(data_source, folder, tableName, dataFrame, selectColumn);
            System.out.println("+++++++++++++++Before Delete Operation++++");
            cleanupSourceTableForLatest(tableName);
            createMergeSql(tableName, columnNames);
            postMergeAction(metaDataCatlog,folderSyncStatus);
        } catch (Exception e) {
            errorFlag++;
            if (errorFlag < 2) {
                try {
                    createMergeSql(tableName, columnNames);
                    postMergeAction(metaDataCatlog, folderSyncStatus);
                } catch (Exception e1) {
                    postMergeActionOnFail(metaDataCatlog, folderSyncStatus);
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }


    private void stageDataFromADLS(String dataSource, String folder, String tableName, String dataFrame, String selectColumn) {
        String query = "INSERT INTO dbo._staging_" + tableName +
                " SELECT " + selectColumn +
                " FROM OPENROWSET(BULK '/" + folder + "/" + tableName + "/*.csv', FORMAT = 'CSV', DATA_SOURCE = '" + dataSource + "') " +
                "WITH (" + dataFrame + ") AS " + tableName;
        System.out.println(query);
        jdbcTemplate.execute(query);
    }

    private void createMergeSql(String tableName, String columnNames) throws SQLException {
        String[] columns = columnNames.split(",");
        StringBuilder updateStatements = new StringBuilder();
        StringBuilder valuesColumns = new StringBuilder();

        for (String col : columns) {
            updateStatements.append("target.").append(col).append(" = source.").append(col).append(",");
            valuesColumns.append("source.").append(col).append(",");
        }

        updateStatements.deleteCharAt(updateStatements.length() - 1);
        valuesColumns.deleteCharAt(valuesColumns.length() - 1);

        String mergeQuery = "MERGE INTO dbo." + tableName + " AS target " +
                "USING dbo._staging_" + tableName + " AS source " +
                "ON target.Id = source.Id " +
                "WHEN MATCHED AND (target.versionnumber < source.versionnumber) THEN " +
                "UPDATE SET " + updateStatements.toString() +
                " WHEN NOT MATCHED BY TARGET THEN " +
                "INSERT (" + columnNames + ") " +
                "VALUES (" + valuesColumns.toString() + ");";

        System.out.println("Executed SQL: " + mergeQuery);
        jdbcTemplate.execute(mergeQuery);
    }

    private void cleanupSourceTableForLatest(String tableName) {
        System.out.println("+++In clean up function +++++++++++++"+tableName);
        String query = "DELETE FROM _staging_"+tableName+"\r\n" + //
                        "WHERE NOT EXISTS (\r\n" + //
                        "    SELECT 1\r\n" + //
                        "    FROM (\r\n" + //
                        "        SELECT id, versionnumber, MAX(sinkmodifiedon) AS latest_modifieddate\r\n" + //
                        "        FROM _staging_"+tableName+"\r\n" + //
                        "        GROUP BY id, versionnumber\r\n" + //
                        "    ) subquery\r\n" + //
                        "    WHERE subquery.id = _staging_"+tableName+".id\r\n" + //
                        "      AND subquery.versionnumber = _staging_"+tableName+".versionnumber\r\n" + //
                        "      AND subquery.latest_modifieddate = _staging_"+tableName+".sinkmodifiedon\r\n" + //
                        ") AND  _staging_"+tableName+".IsDelete NOT in ('1','True')\r\n" + //
                        "";
        int rowsDeleted=jdbcTemplate.update(query);
        System.out.println("Rows Deleted:"+rowsDeleted);
    }

    private void createStagingTable(String tableName, String dataFrame) {
        String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_" + tableName + "') " +
                "DROP TABLE dbo._staging_" + tableName;
        String createTableSQL = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_" + tableName + "') " +
                "CREATE TABLE dbo._staging_" + tableName + "(" + dataFrame + ")";

        jdbcTemplate.execute(dropTableSQL);
        jdbcTemplate.execute(createTableSQL);
    }

    private void postMergeAction(MetaDataCatlog metaDataCatlog,FolderSyncStatus folderSyncStatus) {
        String tableName=metaDataCatlog.getTableName();
        String dropTableSQL = "DROP TABLE IF EXISTS dbo._staging_" + tableName;
        jdbcTemplate.execute(dropTableSQL);

        Short metaCopyStatus=2;
        metaDataCatlog.setLastCopyStatus(metaCopyStatus);
        metaDataCatlog.setLastEndCopyDate(LocalDateTime.now());
        metaDataCatlog.setLastUpdatedFolder(folderSyncStatus.getFolder());
        metaDataCatlogService.save(metaDataCatlog);

        Short copyStatus=1;
        folderSyncStatus.setCopyStatus(copyStatus);
        folderSyncStatusService.save(folderSyncStatus);
    
    }

    private void postMergeActionOnFail(MetaDataCatlog metaDataCatlog,FolderSyncStatus folderSyncStatus) {
        String tableName=metaDataCatlog.getTableName();
        
        String dropTableSQL = "DROP TABLE IF EXISTS dbo._staging_" + tableName;
        jdbcTemplate.execute(dropTableSQL);

        Short metaCopyStatus=3;
        metaDataCatlog.setLastCopyStatus(metaCopyStatus);
        metaDataCatlog.setLastEndCopyDate(LocalDateTime.now());
        metaDataCatlog.setLastUpdatedFolder(folderSyncStatus.getFolder());
        metaDataCatlogService.save(metaDataCatlog);

        Short copyStatus=2;
        folderSyncStatus.setCopyStatus(copyStatus);
        folderSyncStatusService.save(folderSyncStatus);

    }
}
