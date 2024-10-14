package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class PolybaseThreadService implements Runnable {

    private final String tableName;
    private final String folder;





    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PropertiesService propertyService;

    public PolybaseThreadService(String tableName, String folder) {
        this.tableName = tableName;
        this.folder = folder;
    }

     public void runTask(String tableName, String folder) {
        // Your processing logic here, using tableName and folder
        run(); // or other methods to process
    }

    @Override
    public void run() {
        Map<String, String> schemaString = getMetaDataCatlog(tableName);
        String dataFrame = schemaString.get("dataFrame");
        String selectColumn = schemaString.get("selectColumn");
        String columnNames = schemaString.get("columnNames");

        int errorFlag = 0;
        createStagingTable(tableName, dataFrame);
        stageDataFromADLS(propertyService.getPropertyValue("DATA_SOURCE"), folder, tableName, dataFrame, selectColumn);
        try {
            createMergeSql(tableName, columnNames);
            postMergeAction(tableName, folder);
        } catch (SQLException e) {
            errorFlag++;
            if (errorFlag < 2) {
                try {
                    cleanupSourceTableForLatest(tableName);
                    createMergeSql(tableName, columnNames);
                    postMergeAction(tableName, folder);
                } catch (SQLException e1) {
                    postMergeActionOnFail(tableName, folder);
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    private Map<String, String> getMetaDataCatlog(String tableName) {
        String query = "SELECT TableName, DataFrame, SelectColumn, ColumnNames FROM MetaDataCatlog WHERE TableName = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{tableName}, (rs, rowNum) -> {
            Map<String, String> metaDataCatlog = new HashMap<>();
            metaDataCatlog.put("tName", rs.getString("TableName"));
            metaDataCatlog.put("dataFrame", rs.getString("DataFrame"));
            metaDataCatlog.put("selectColumn", rs.getString("SelectColumn"));
            metaDataCatlog.put("columnNames", rs.getString("ColumnNames"));
            return metaDataCatlog;
        });
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
        String query = "SELECT Id, MAX(versionnumber) AS versionnumber FROM dbo." + tableName +
                " WHERE IsDelete NOT IN ('1','True') GROUP BY Id HAVING COUNT(Id) > 1";
        jdbcTemplate.query(query, (rs) -> {
            String Id = rs.getString("Id");
            String versionnumber = rs.getString("versionnumber");
            String deleteQuery = "DELETE FROM dbo." + tableName + " WHERE Id = ? AND versionnumber < ?";
            jdbcTemplate.update(deleteQuery, Id, versionnumber);
        });
    }

    private void createStagingTable(String tableName, String dataFrame) {
        String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_" + tableName + "') " +
                "DROP TABLE dbo._staging_" + tableName;
        String createTableSQL = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_" + tableName + "') " +
                "CREATE TABLE dbo._staging_" + tableName + "(" + dataFrame + ")";

        jdbcTemplate.execute(dropTableSQL);
        jdbcTemplate.execute(createTableSQL);
    }

    private void postMergeAction(String tableName, String folder) {
        String dropTableSQL = "DROP TABLE IF EXISTS dbo._staging_" + tableName;
        jdbcTemplate.execute(dropTableSQL);

        String updateSQL = "UPDATE MetaDataCatlog SET LastEndCopyDate = ?, LastCopyStatus = ?, LastUpdatedFolder = ? WHERE TableName = ?";
        jdbcTemplate.update(updateSQL, new Timestamp(System.currentTimeMillis()), 2, folder, tableName);

        String updateSyncStatusSQL = "UPDATE FolderSyncStatus SET copystatus = ? WHERE folder = ? AND tablename = ?";
        jdbcTemplate.update(updateSyncStatusSQL, 1, folder, tableName);
    }

    private void postMergeActionOnFail(String tableName, String folder) {
        String dropTableSQL = "DROP TABLE IF EXISTS dbo._staging_" + tableName;
        jdbcTemplate.execute(dropTableSQL);

        String updateSQL = "UPDATE MetaDataCatlog SET LastEndCopyDate = ?, LastCopyStatus = ?, LastUpdatedFolder = ? WHERE TableName = ?";
        jdbcTemplate.update(updateSQL, null, 3, folder, tableName);

        String updateSyncStatusSQL = "UPDATE FolderSyncStatus SET copystatus = ? WHERE folder = ? AND tablename = ?";
        jdbcTemplate.update(updateSyncStatusSQL, 2, folder, tableName);
    }
}
