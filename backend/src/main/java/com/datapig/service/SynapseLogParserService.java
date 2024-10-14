package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SynapseLogParserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PolybaseService polybaseService;

    public void startParse(String filePath) {
        Set<String> logEntries = readSynapseLog(filePath);
        Set<String> dbFolders = allFoldersInDB();

        // Insert new folders from the log that are not in DB
        logEntries.stream().filter(folder -> !dbFolders.contains(folder))
                .forEach(this::insertChangeLog);

        Set<String> existingFoldersNotStaged = existingFolderNotStaged();

        for (String folderName : existingFoldersNotStaged) {
            Set<String> tableNames = getTableInFolder(folderName);
            for (String tbl : tableNames) {
                insertFolderSyncStatus(folderName, tbl);
            }
            updateChangeLog(folderName, 1);  // Update the status after processing
        }

        boolean flag = true;
        while (flag) {
            String folder = foldersNeedToBeProcessed();
            if (folder != null) {
                // Simulate Polybase processing
                polybaseService.startSyncInFolder(folder);
                updateChangeLogToComplete(folder);
                if (countFoldersNeedToBeProcessed() == 0) {
                    flag = false;
                }
            }
        }
    }

    private int countFoldersNeedToBeProcessed() {
        String query = "SELECT COUNT(*) FROM ChangeLog WHERE stagestatus = 1";
        return jdbcTemplate.queryForObject(query, Integer.class);
    }

    private String foldersNeedToBeProcessed() {
        String query = "SELECT foldername FROM ChangeLog WHERE stagetime = " +
                "(SELECT MIN(stagetime) FROM ChangeLog WHERE stagestatus = 1 " +
                "AND foldername IN (SELECT folder FROM FolderSyncStatus WHERE copystatus = 0))";
        return jdbcTemplate.queryForObject(query, String.class);
    }

    private void updateChangeLogToComplete(String folderName) {
        String query = "UPDATE ChangeLog SET stagestatus = 2, stagetime = ? WHERE foldername = ?";
        jdbcTemplate.update(query, new Timestamp(System.currentTimeMillis()), folderName);
    }

    private void updateChangeLog(String folderName, int stageStatus) {
        String query = "UPDATE ChangeLog SET stagestatus = ?, stagetime = ? WHERE foldername = ?";
        jdbcTemplate.update(query, stageStatus, new Timestamp(System.currentTimeMillis()), folderName);
    }

    private void insertFolderSyncStatus(String folderName, String tableName) {
        String query = "INSERT INTO FolderSyncStatus (folder, tablename, copystatus) VALUES (?, ?, 0)";
        jdbcTemplate.update(query, folderName, tableName);
    }

    private Set<String> getTableInFolder(String folderName) {
        // SQL logic to extract the table names
        String query = "/* Your OpenRowSet query here */";
        List<String> tableNames = jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("tablename"));
        return new LinkedHashSet<>(tableNames);
    }

    private Set<String> allFoldersInDB() {
        String query = "SELECT foldername FROM ChangeLog ORDER BY stagetime ASC";
        List<String> folderNames = jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("foldername"));
        return new LinkedHashSet<>(folderNames);
    }

    private Set<String> existingFolderNotStaged() {
        String query = "SELECT foldername FROM ChangeLog WHERE stagestatus = 0 ORDER BY stagetime ASC";
        List<String> folderNames = jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("foldername"));
        return new LinkedHashSet<>(folderNames);
    }

    private void insertChangeLog(String folderName) {
        String query = "INSERT INTO ChangeLog (foldername, stagestatus, stagetime) VALUES (?, 0, ?)";
        jdbcTemplate.update(query, folderName, new Timestamp(System.currentTimeMillis()));
    }

    private Set<String> readSynapseLog(String filePath) {
       
        Set<String> set = new LinkedHashSet<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read the file line by line
            while ((line = br.readLine()) != null) {
                // Split the line using comma as delimiter
                String[] parts = line.split(",");
                // Add each part to the set
                for (String part : parts) {
                    set.add(part.trim()); // trim to remove leading/trailing spaces
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Print the set
        System.out.println("Set contents: " + set);
        return set;// Return a dummy set for now
    }
}

