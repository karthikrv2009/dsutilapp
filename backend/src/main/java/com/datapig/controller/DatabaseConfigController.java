package com.datapig.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.datapig.component.DataPigValidator;
import com.datapig.component.DynamicDataSourceManager;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.IntialLoad;
import com.datapig.repository.IntitalLoadRepository;

import com.datapig.service.AzureQueueListenerService;
import com.datapig.service.CDCLoaderService;
import com.datapig.service.DatabaseConfigService;
import com.datapig.service.InitialLoadService;
import com.datapig.service.MetaDataCatlogService;
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
import java.util.stream.Collectors;

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

    @Autowired
    private MetaDataCatlogService metaDataCatlogService;

    @Autowired
    private DataPigValidator dataPigValidator;
    
    @Autowired
    private CDCLoaderService cDCLoaderService;

    @GetMapping
    public ResponseEntity<List<DatabaseConfig>> getAllDatabaseConfigs() {
        List<DatabaseConfig> configs = databaseConfigService.getAllDatabaseConfigs();
        return ResponseEntity.ok(configs);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateDatabaseConfig(@RequestBody DatabaseConfig config) {
        Map<String, Boolean> validationResults = new HashMap<>();
    
        // Perform null/empty validation checks for fields (before calling the validation methods)
        validationResults.put("url", config.getUrl() != null && !config.getUrl().isEmpty());
        validationResults.put("username", config.getUsername() != null && !config.getUsername().isEmpty());
        validationResults.put("password", config.getPassword() != null && !config.getPassword().isEmpty());
        validationResults.put("dbIdentifier", config.getDbIdentifier() != null && !config.getDbIdentifier().isEmpty());
        validationResults.put("driverClassName", config.getDriverClassName() != null && !config.getDriverClassName().isEmpty());
        validationResults.put("queueName", config.getQueueName() != null && !config.getQueueName().isEmpty());
        validationResults.put("queueSasToken", config.getQueueSasToken() != null && !config.getQueueSasToken().isEmpty());
        validationResults.put("queueEndpoint", config.getQueueEndpoint() != null && !config.getQueueEndpoint().isEmpty());
        validationResults.put("adlsStorageAccountName", config.getAdlsStorageAccountName() != null && !config.getAdlsStorageAccountName().isEmpty());
        validationResults.put("adlsStorageAccountEndpoint", config.getAdlsStorageAccountEndpoint() != null && !config.getAdlsStorageAccountEndpoint().isEmpty());
        validationResults.put("adlsStorageAccountSasKey", config.getAdlsStorageAccountSasKey() != null && !config.getAdlsStorageAccountSasKey().isEmpty());
        validationResults.put("adlsContainerName", config.getAdlsContainerName() != null && !config.getAdlsContainerName().isEmpty());
        validationResults.put("adlsFolderName", config.getAdlsFolderName() != null && !config.getAdlsFolderName().isEmpty());
        validationResults.put("adlsCdmFileName", config.getAdlsCdmFileName() != null && !config.getAdlsCdmFileName().isEmpty());
        validationResults.put("adlsCdmFilePath", config.getAdlsCdmFilePath() != null && !config.getAdlsCdmFilePath().isEmpty());
        validationResults.put("localCdmFilePath", config.getLocalCdmFilePath() != null && !config.getLocalCdmFilePath().isEmpty());
        validationResults.put("maxThreads", config.getMaxThreads() > 0);
    
        // Validate DB connection
        boolean dbConnectionValid = dataPigValidator.checkDBConnection(config.getUrl(), config.getUsername(), config.getPassword(), config.getDriverClassName());
        if (!dbConnectionValid) {
            validationResults.put("url", false);
            validationResults.put("username", false);
            validationResults.put("password", false);
            validationResults.put("dbIdentifier", false);
            validationResults.put("driverClassName", false);
        } else {
            validationResults.put("dbConnection", true);  // DB connection is valid
        }
    
        // Validate ADLS Queue connection
        boolean adlsQueueConnectionValid = dataPigValidator.checkADLSQueueConnection(config.getQueueName(), config.getQueueEndpoint(), config.getQueueSasToken());
        if (!adlsQueueConnectionValid) {
            validationResults.put("queueName", false);
            validationResults.put("queueEndpoint", false);
            validationResults.put("queueSasToken", false);
        } else {
            validationResults.put("adlsQueueConnection", true);  // ADLS Queue connection is valid
        }
    
        // Check if the model.json exists in the ADLS container
        boolean modelJsonExistenceValid = dataPigValidator.checkModelJsonExistence(config.getAdlsStorageAccountEndpoint(), config.getAdlsContainerName(), config.getAdlsStorageAccountSasKey());
        if (!modelJsonExistenceValid) {
            validationResults.put("adlsContainerName", false);
            validationResults.put("adlsStorageAccountSasKey", false);
            validationResults.put("adlsStorageAccountEndpoint", false);
        } else {
            validationResults.put("modelJsonExistence", true);  // Model.json exists in ADLS
        }
    
        // Validate local folder path
        boolean localFilePathValid = dataPigValidator.checkFilePathExist(config.getLocalCdmFilePath());
        if (!localFilePathValid) {
            validationResults.put("localCdmFilePath", false);
        } else {
            validationResults.put("localFilePath", true);  // Local file path is valid
        }
    
        // After all validations are done, return the validation results map
        return ResponseEntity.ok(validationResults);
    }
    
    @PostMapping("/save")
    public ResponseEntity<DatabaseConfigDTO> saveDatabaseConfig(@RequestBody DatabaseConfig databaseConfig) {
        dynamicDataSourceManager.addDataSource(databaseConfig.getDbIdentifier(), databaseConfig.getUrl(),
                databaseConfig.getUsername(), databaseConfig.getPassword());

        IntialLoad intialLoad = intitalLoadRepository.findByDbIdentifier(databaseConfig.getDbIdentifier());

        DatabaseConfigDTO databaseConfigDTO = new DatabaseConfigDTO();

        databaseConfigDTO = databaseConfigDTO.fromEntity(databaseConfig, intialLoad.getStagestatus(),
                intialLoad.getStagestatus());

        databaseConfigService.saveDatabaseConfig(databaseConfig);

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
            String createdTableName = null;
            cDCLoaderService.loadCDC(dbProfile, table, startTime, endTime);;

            Map<String, String> response = new HashMap<>();
            response.put("tableName", createdTableName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateDatabaseConfig(@PathVariable Long id, @RequestBody DatabaseConfig config) {
        try {
            databaseConfigService.updateDatabaseConfig(id, config);
            return ResponseEntity.ok("Database config updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating database config");
        }
    }

    @GetMapping("/tables")
    public ResponseEntity<List<String>> getTables(@RequestParam String dbProfile) {
        try {
            List<String> tables = metaDataCatlogService.getAllTableNamesByDbIdentifier(dbProfile).stream()
                    .collect(Collectors.toList());// databaseConfigService.getTables(dbProfile);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}