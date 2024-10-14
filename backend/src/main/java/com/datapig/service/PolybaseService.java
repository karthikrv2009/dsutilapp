package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class PolybaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PolybaseThreadService polybaseThreadService;


    // Replace the old instantiation like this:
    public void startPolybaseProcess(String tableName, String folderName) {
    // Assuming your PolybaseThreadService requires certain parameters
    polybaseThreadService.runTask(tableName, folderName);
    }

    public void startSyncInFolder(String folderName) {
        Set<String> tablesInFolder = getTableNamesByFolder(folderName);

        // Create an ExecutorService with a fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(tablesInFolder.size());

        for (String tableName : tablesInFolder) {
            preMergeAction(tableName, folderName);
            polybaseThreadService= new PolybaseThreadService(tableName, folderName);
            startPolybaseProcess(tableName, folderName);
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

        System.out.println("All data merge tasks completed for folder: " + folderName);
    }

    private void preMergeAction(String tableName, String folder) {
        String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_" + tableName + "') " +
                "BEGIN " +
                "DROP TABLE dbo._staging_" + tableName + " " +
                "END";

        jdbcTemplate.execute(dropTableSQL);

        String updateSQL = "UPDATE MetaDataCatlog SET LastStartCopyDate = ?, LastCopyStatus = ?, LastUpdatedFolder = ? WHERE TableName = ?";
        jdbcTemplate.update(updateSQL, new Timestamp(System.currentTimeMillis()), 1, folder, tableName);
    }

    private Set<String> getTableNamesByFolder(String folderName) {
        String query = "SELECT tablename FROM FolderSyncStatus WHERE folder = ? AND copystatus = 0";
        return new LinkedHashSet<>(jdbcTemplate.query(query, new Object[]{folderName}, (rs, rowNum) -> rs.getString("tablename")));
    }
}

