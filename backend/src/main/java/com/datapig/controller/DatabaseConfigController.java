package com.datapig.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datapig.entity.DatabaseConfig;
import com.datapig.service.DatabaseConfigService;

@RestController
@RequestMapping("/api/profile")
public class DatabaseConfigController {

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @GetMapping("/getAllDatabaseConfigs")
    public ResponseEntity<List<DatabaseConfig>> getAllDatabaseConfigs() {
        List<DatabaseConfig> databaseConfigs = databaseConfigService.getAllDatabaseConfigs();
        return ResponseEntity.ok(databaseConfigs);
    }

    @GetMapping("/getDatabaseConfigByIdentifier/{dbIdentifier}")
    public ResponseEntity<DatabaseConfig> getDatabaseConfigByIdentifier(@PathVariable String dbIdentifier) {
        DatabaseConfig databaseConfig = databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
        return ResponseEntity.ok(databaseConfig);
    }

    @PostMapping("/saveDatabaseConfig")
    public ResponseEntity<DatabaseConfig> saveDatabaseConfig(@RequestBody DatabaseConfig databaseConfig) {
        databaseConfigService.saveDatabaseConfig(databaseConfig);
        return ResponseEntity.ok(databaseConfig);
    }

    @DeleteMapping("/deleteDatabaseConfig")
    public ResponseEntity<DatabaseConfig> deleteDatabaseConfig(@RequestBody DatabaseConfig databaseConfig) {
        databaseConfigService.deleteDatabaseConfig(databaseConfig);
        return ResponseEntity.ok(databaseConfig);
    }

}
