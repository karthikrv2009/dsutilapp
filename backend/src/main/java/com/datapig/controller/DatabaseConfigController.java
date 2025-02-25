package com.datapig.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.datapig.component.DataPigValidator;
import com.datapig.component.DynamicDataSourceManager;
import com.datapig.entity.ChangeDataTracking;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.IntialLoad;
import com.datapig.entity.LicenseKey;
import com.datapig.repository.IntitalLoadRepository;

import com.datapig.service.AzureQueueListenerService;
import com.datapig.service.CDCLoaderService;
import com.datapig.service.ChangeDataTrackingService;
import com.datapig.service.DatabaseConfigService;
import com.datapig.service.InitialLoadService;
import com.datapig.service.LicenseKeyService;
import com.datapig.service.MetaDataCatlogService;
import com.datapig.service.dto.DatabaseConfigDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Autowired
    private LicenseKeyService licenseKeyService;

    @Autowired
    private ChangeDataTrackingService changeDataTrackingService;

    @GetMapping
    public ResponseEntity<List<DatabaseConfigDTO>> getAllDatabaseConfigs() {
        List<DatabaseConfig> configs = databaseConfigService.getAllDatabaseConfigs();

        List<DatabaseConfigDTO> dtos = new ArrayList<>();
        for (DatabaseConfig config : configs) {
            DatabaseConfigDTO databaseConfigDTO = new DatabaseConfigDTO();
            IntialLoad intialLoad = intitalLoadRepository.findByDbIdentifier(config.getDbIdentifier());
            if (intialLoad != null) {
                dtos.add(databaseConfigDTO.fromEntity(config, intialLoad.getStatus(),
                        intialLoad.getQueueListenerStatus()));
            } else {
                dtos.add(databaseConfigDTO.fromEntity(config, 0, 0));
            }

        }

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateDatabaseConfig(@RequestBody DatabaseConfig config) {
        Map<String, Boolean> validationResults = new HashMap<>();

        // Perform null/empty validation checks for fields (before calling the
        // validation methods)
        System.out.println(config.toString());
        validationResults.put("url", config.getUrl() != null && !config.getUrl().isEmpty());
        validationResults.put("username", config.getUsername() != null && !config.getUsername().isEmpty());
        validationResults.put("password", config.getPassword() != null && !config.getPassword().isEmpty());
        validationResults.put("dbIdentifier", config.getDbIdentifier() != null && !config.getDbIdentifier().isEmpty());
        validationResults.put("driverClassName",
                config.getDriverClassName() != null && !config.getDriverClassName().isEmpty());
        validationResults.put("queueName", config.getQueueName() != null && !config.getQueueName().isEmpty());
        validationResults.put("queueSasToken",
                config.getQueueSasToken() != null && !config.getQueueSasToken().isEmpty());
        validationResults.put("queueEndpoint",
                config.getQueueEndpoint() != null && !config.getQueueEndpoint().isEmpty());
        validationResults.put("adlsStorageAccountName",
                config.getAdlsStorageAccountName() != null && !config.getAdlsStorageAccountName().isEmpty());
        validationResults.put("adlsStorageAccountEndpoint",
                config.getAdlsStorageAccountEndpoint() != null && !config.getAdlsStorageAccountEndpoint().isEmpty());
        validationResults.put("adlsStorageAccountSasKey",
                config.getAdlsStorageAccountSasKey() != null && !config.getAdlsStorageAccountSasKey().isEmpty());
        validationResults.put("adlsContainerName",
                config.getAdlsContainerName() != null && !config.getAdlsContainerName().isEmpty());
        validationResults.put("adlsFolderName",
                config.getAdlsFolderName() != null && !config.getAdlsFolderName().isEmpty());
        validationResults.put("adlsCdmFileName",
                config.getAdlsCdmFileName() != null && !config.getAdlsCdmFileName().isEmpty());
        validationResults.put("adlsCdmFilePath",
                config.getAdlsCdmFilePath() != null && !config.getAdlsCdmFilePath().isEmpty());
        validationResults.put("localCdmFilePath",
                config.getLocalCdmFilePath() != null && !config.getLocalCdmFilePath().isEmpty());
        validationResults.put("maxThreads", config.getMaxThreads() > 0);

       
        // Validate DB connection
        boolean dbConnectionValid = dataPigValidator.checkDBConnection(config.getUrl(), config.getUsername(),
                config.getPassword(), config.getDriverClassName());
        if (!dbConnectionValid) {
            validationResults.put("url", false);
            validationResults.put("username", false);
            validationResults.put("password", false);
            validationResults.put("dbIdentifier", false);
            validationResults.put("driverClassName", false);
        } else {
            validationResults.put("dbConnection", true); // DB connection is valid
        }

        // Validate ADLS Queue connection
        boolean adlsQueueConnectionValid = dataPigValidator.checkADLSQueueConnection(config.getQueueName(),
                config.getQueueEndpoint(), config.getQueueSasToken());
        if (!adlsQueueConnectionValid) {
            validationResults.put("queueName", false);
            validationResults.put("queueEndpoint", false);
            validationResults.put("queueSasToken", false);
        } else {
            validationResults.put("adlsQueueConnection", true); // ADLS Queue connection is valid
        }

        // Check if the model.json exists in the ADLS container
        boolean modelJsonExistenceValid = dataPigValidator.checkModelJsonExistence(
                config.getAdlsStorageAccountEndpoint(), config.getAdlsContainerName(),
                config.getAdlsStorageAccountSasKey());
        
        if (!modelJsonExistenceValid) {
            validationResults.put("adlsContainerName", false);
            validationResults.put("adlsStorageAccountSasKey", false);
            validationResults.put("adlsStorageAccountEndpoint", false);
        } else {
            validationResults.put("modelJsonExistence", true); // Model.json exists in ADLS
        }

        // Validate local folder path
        boolean localFilePathValid = dataPigValidator.checkFilePathExist(config.getLocalCdmFilePath());
        if (!localFilePathValid) {
            validationResults.put("localCdmFilePath", false);
        } else {
            validationResults.put("localFilePath", true); // Local file path is valid
        }

        // After all validations are done, return the validation results map
        return ResponseEntity.ok(validationResults);
    }

    @PostMapping("/save")
    public ResponseEntity<DatabaseConfigDTO> saveDatabaseConfig(@RequestBody DatabaseConfig databaseConfig) {

        databaseConfig.setPurgeDuration(getPurgeDurationInMilliseconds(databaseConfig));
        dynamicDataSourceManager.addDataSource(databaseConfig.getDbIdentifier(), databaseConfig.getUrl(),
                databaseConfig.getUsername(), databaseConfig.getPassword());

        IntialLoad intialLoad = intitalLoadRepository.findByDbIdentifier(databaseConfig.getDbIdentifier());

        DatabaseConfigDTO databaseConfigDTO = new DatabaseConfigDTO();

        List<LicenseKey> licenseKeys = licenseKeyService.getAllLicenseKeys();

        databaseConfig.setAdlsContainerName(licenseKeys.get(0).getEnvironment());
        databaseConfig.setAdlsFolderName(licenseKeys.get(0).getEnvironment());

        if (intialLoad != null) {
            databaseConfigDTO = databaseConfigDTO.fromEntity(databaseConfig, intialLoad.getStagestatus(),
                    intialLoad.getStagestatus());
        } else {
            databaseConfigDTO = databaseConfigDTO.fromEntity(databaseConfig, 0,
                    0);
            intialLoad = new IntialLoad();
            intialLoad.setDbIdentifier(databaseConfigDTO.getDbIdentifier());
            intialLoad.setStatus(0);
            intialLoad.setStagestatus(0);
            intialLoad.setStageendtime(null);
            intialLoad.setStagestarttime(null);
            intialLoad.setName(databaseConfigDTO.getDbIdentifier());
            intialLoad.setQueueListenerStatus(0);
            intitalLoadRepository.save(intialLoad);
        }
        System.out.println(databaseConfig.toString());
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
            ChangeDataTracking changeDataTracking = cDCLoaderService.loadCDC(dbProfile, table, startTime, endTime);
            if (changeDataTracking != null) {
                createdTableName = "CDC Table for " + changeDataTracking.getTableName() + " : "
                        + changeDataTracking.getCdcTableName() + " Processing started.";
            } else {
                createdTableName = "No records for table " + table
                        + " found in ADLS on range specified. Update and retry.";
            }

            Map<String, String> response = new HashMap<>();
            response.put("tableName", createdTableName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateDatabaseConfig(@PathVariable Long id, @RequestBody DatabaseConfig request) {
        try {
            System.out.println(request.toString());
            request.setPurgeDuration(getPurgeDurationInMilliseconds(request));
            databaseConfigService.updateDatabaseConfig(id, request);
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

    @GetMapping("/change-data-tracking")
    public ResponseEntity<List<ChangeDataTracking>> getChangeDataTrackingService() {
        return ResponseEntity.ok(changeDataTrackingService.findAll());
    }

    public DatabaseConfigDTO toDTO(DatabaseConfig databaseConfig, int initialLoadStatus, int queueListenerStatus) {
        DatabaseConfigDTO dto = new DatabaseConfigDTO();
        dto.setId(databaseConfig.getId());
        dto.setUrl(databaseConfig.getUrl());
        dto.setUsername(databaseConfig.getUsername());
        dto.setPassword(databaseConfig.getPassword());
        dto.setDbIdentifier(databaseConfig.getDbIdentifier());
        dto.setDriverClassName(databaseConfig.getDriverClassName());
        dto.setQueueName(databaseConfig.getQueueName());
        dto.setQueueSasToken(databaseConfig.getQueueSasToken());
        dto.setQueueEndpoint(databaseConfig.getQueueEndpoint());
        dto.setAdlsStorageAccountName(databaseConfig.getAdlsStorageAccountName());
        dto.setAdlsStorageAccountEndpoint(databaseConfig.getAdlsStorageAccountEndpoint());
        dto.setAdlsStorageAccountSasKey(databaseConfig.getAdlsStorageAccountSasKey());
        dto.setAdlsContainerName(databaseConfig.getAdlsContainerName());
        dto.setAdlsFolderName(databaseConfig.getAdlsFolderName());
        dto.setAdlsCdmFileName(databaseConfig.getAdlsCdmFileName());
        dto.setAdlsCdmFilePath(databaseConfig.getAdlsCdmFilePath());
        dto.setLocalCdmFilePath(databaseConfig.getLocalCdmFilePath());
        dto.setMaxThreads(databaseConfig.getMaxThreads());
        dto.setEnableArchive(databaseConfig.isEnableArchive());
        dto.setInitialLoadStatus(initialLoadStatus);
        dto.setDefaultProfile(databaseConfig.isDefaultProfile());
        dto.setEnableArchive(databaseConfig.isPurgeEnabled());
        dto.setPurgeDuration(databaseConfig.getPurgeDuration());
        dto.setQueueListenerStatus(queueListenerStatus);
        return dto;
    }

    public long getPurgeDurationInMilliseconds(DatabaseConfig config) {
        long durationInMilliseconds = 0;
        switch (config.getPurgeUnit().toLowerCase()) {
            case "weeks":
                durationInMilliseconds = config.getPurgeUnitValue() * 7L * 24L * 60L * 60L * 1000L;
                break;
            case "days":
                durationInMilliseconds = config.getPurgeUnitValue() * 24L * 60L * 60L * 1000L;
                break;
            case "months":
                durationInMilliseconds = config.getPurgeUnitValue() * 30L * 24L * 60L * 60L * 1000L;
                break;
            case "years":
                durationInMilliseconds = config.getPurgeUnitValue() * 365L * 24L * 60L * 60L * 1000L;
                break;
            default:
                throw new IllegalArgumentException("Invalid purge unit: " + config.getPurgeUnit().toLowerCase());
        }
        return durationInMilliseconds;
    }
}