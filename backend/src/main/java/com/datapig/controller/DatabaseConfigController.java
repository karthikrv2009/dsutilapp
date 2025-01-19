package com.datapig.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.datapig.component.DynamicDataSourceManager;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.IntialLoad;
import com.datapig.repository.IntitalLoadRepository;

import com.datapig.service.AzureQueueListenerService;
import com.datapig.service.DatabaseConfigService;
import com.datapig.service.InitialLoadService;
import com.datapig.service.dto.DatabaseConfigDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database-configs")
public class DatabaseConfigController {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigController.class);
    @Autowired
    IntitalLoadRepository intitalLoadRepository;

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Autowired
    private InitialLoadService initialLoadService;

    @Autowired
    private DynamicDataSourceManager dynamicDataSourceManager;

    @Autowired
    private AzureQueueListenerService azureQueueListenerService;

    @GetMapping
    public ResponseEntity<List<DatabaseConfig>> getAllDatabaseConfigs() {
        List<DatabaseConfig> configs = databaseConfigService.getAllDatabaseConfigs();
        return ResponseEntity.ok(configs);
    }

    @PostMapping("/save")
    public ResponseEntity<DatabaseConfigDTO> saveDatabaseConfig(@RequestBody DatabaseConfig databaseConfig) {
        dynamicDataSourceManager.addDataSource(databaseConfig.getDbIdentifier(), databaseConfig.getUrl(),
                databaseConfig.getUsername(), databaseConfig.getPassword());

        IntialLoad intialLoad = intitalLoadRepository.findByDbIdentifier(databaseConfig.getDbIdentifier());

        DatabaseConfigDTO databaseConfigDTO = new DatabaseConfigDTO();

        databaseConfigDTO = databaseConfigDTO.fromEntity(databaseConfig, intialLoad.getStagestatus(),
                intialLoad.getStagestatus());

        return ResponseEntity.ok(databaseConfigDTO);

    }

    @PostMapping("/start-initial-load")
    public ResponseEntity<String> startInitialLoad(@RequestBody String request) {
        logger.info("Initial Load started successfully :" + request);
        JsonObject jsonObject = JsonParser.parseString(request).getAsJsonObject();
        String dbIdentifier = jsonObject.get("dbIdentifier").getAsString();
        initialLoadService.runInitialLoad(dbIdentifier);
        return ResponseEntity.ok("Initial Load started successfully");
    }

    @PostMapping("/start-queue-listener")
    public ResponseEntity<String> startQueueListener(@RequestBody String request) {
        logger.info("Queue Listener started successfully" + request);
        JsonObject jsonObject = JsonParser.parseString(request).getAsJsonObject();
        String dbIdentifier = jsonObject.get("dbIdentifier").getAsString();
        azureQueueListenerService.startQueueListener(dbIdentifier);

        return ResponseEntity.ok("Queue Listener started successfully");
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitForm(@RequestBody Map<String, String> payload) {
        try {
            String dbProfile = payload.get("dbProfile");
            String table = payload.get("table");
            String startTimeStr = payload.get("startTime");
            String endTimeStr = payload.get("endTime");

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);

            // Process the payload and create the table
            String createdTableName = null; // databaseConfigService.createTable(dbProfile, table, startTime, endTime);

            Map<String, String> response = new HashMap<>();
            response.put("tableName", createdTableName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/tables")
    public ResponseEntity<List<String>> getTables(@RequestParam String dbProfile) {
        try {
            List<String> tables = null;// databaseConfigService.getTables(dbProfile);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}