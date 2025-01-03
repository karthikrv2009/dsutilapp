package com.datapig.controller;

import com.datapig.component.DynamicDataSourceManager;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.IntialLoad;
import com.datapig.repository.IntitalLoadRepository;
import com.datapig.service.AzureQueueListenerService;
import com.datapig.service.DatabaseConfigService;
import com.datapig.service.InitialLoadService;
import com.datapig.service.dto.DatabaseConfigDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/database-configs")
public class DatabaseConfigController {

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
        DatabaseConfig savedConfig = databaseConfigService.saveDatabaseConfig(databaseConfig);
        dynamicDataSourceManager.addDataSource(databaseConfig.getDbIdentifier(), databaseConfig.getUrl(),
                databaseConfig.getUsername(), databaseConfig.getPassword());

        IntialLoad intialLoad = intitalLoadRepository.findByDbIdentifier(databaseConfig.getDbIdentifier());

        DatabaseConfigDTO databaseConfigDTO = new DatabaseConfigDTO();

        databaseConfigDTO = databaseConfigDTO.fromEntity(databaseConfig, intialLoad.getStagestatus(),
                intialLoad.getStagestatus());

        return ResponseEntity.ok(databaseConfigDTO);

    }

    @PostMapping("/start-initial-load")
    public ResponseEntity<String> startInitialLoad(@RequestBody String dbIdentifier) {
        initialLoadService.runInitialLoad(dbIdentifier);
        return ResponseEntity.ok("Initial Load started successfully");
    }

    @PostMapping("/start-queue-listener")
    public ResponseEntity<String> startQueueListener(@RequestBody String dbIdentifier) {
        azureQueueListenerService.startQueueListener(dbIdentifier);

        return ResponseEntity.ok("Queue Listener started successfully");
    }
}