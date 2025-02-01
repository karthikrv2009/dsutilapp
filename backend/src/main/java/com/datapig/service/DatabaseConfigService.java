package com.datapig.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.component.DynamicDataSourceManager;
import com.datapig.entity.DatabaseConfig;

import com.datapig.repository.DatabaseConfigRepository;

@Service
public class DatabaseConfigService {

    @Autowired
    private DatabaseConfigRepository databaseConfigRepository;

    @Autowired
    private DynamicDataSourceManager dynamicDataSourceManager;

    public List<DatabaseConfig> getAllDatabaseConfigs() {
        return databaseConfigRepository.findAll();
    }

    public DatabaseConfig getDatabaseConfigByIdentifier(String dbIdentifier) {
        System.out.println("Fetching DatabaseConfig for identifier: " + dbIdentifier);
        DatabaseConfig result = databaseConfigRepository.findByDbIdentifier(dbIdentifier);
        if (result == null) {
            System.out.println("No DatabaseConfig found for identifier: " + dbIdentifier);
        }
        return result;
    }

    public DatabaseConfig saveDatabaseConfig(DatabaseConfig databaseConfig) {
        DatabaseConfig newDatabaseConfig = databaseConfigRepository.save(databaseConfig);
        dynamicDataSourceManager.addDataSource(databaseConfig.getDbIdentifier(), databaseConfig.getUrl(),
                databaseConfig.getUsername(), databaseConfig.getPassword());
        return newDatabaseConfig;

    }

    public void updateDatabaseConfig(Long id, DatabaseConfig config) {
        DatabaseConfig existingConfig = databaseConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found"));
        existingConfig.setUrl(config.getUrl());
        existingConfig.setUsername(config.getUsername());
        existingConfig.setPassword(config.getPassword());
        existingConfig.setDbIdentifier(config.getDbIdentifier());
        existingConfig.setDriverClassName(config.getDriverClassName());
        existingConfig.setQueueName(config.getQueueName());
        existingConfig.setQueueSasToken(config.getQueueSasToken());
        existingConfig.setQueueEndpoint(config.getQueueEndpoint());
        existingConfig.setAdlsStorageAccountName(config.getAdlsStorageAccountName());
        existingConfig.setAdlsStorageAccountEndpoint(config.getAdlsStorageAccountEndpoint());
        existingConfig.setAdlsStorageAccountSasKey(config.getAdlsStorageAccountSasKey());
        existingConfig.setAdlsContainerName(config.getAdlsContainerName());
        existingConfig.setAdlsFolderName(config.getAdlsFolderName());
        existingConfig.setAdlsCdmFileName(config.getAdlsCdmFileName());
        existingConfig.setAdlsCdmFilePath(config.getAdlsCdmFilePath());
        existingConfig.setLocalCdmFilePath(config.getLocalCdmFilePath());
        existingConfig.setMaxThreads(config.getMaxThreads());
        existingConfig.setEnableArchive(config.isEnableArchive());
        existingConfig.setDefaultProfile(config.isDefaultProfile());
        DatabaseConfig databaseConfig=databaseConfigRepository.save(existingConfig);
        if(databaseConfig.isDefaultProfile()){
            updateDatabaseConfigs(databaseConfig.getDbIdentifier());
        }
    }
    public void deleteDatabaseConfig(DatabaseConfig databaseConfig) {
        databaseConfigRepository.deleteById(databaseConfig.getId());
        dynamicDataSourceManager.removeDataSource(databaseConfig.getDbIdentifier());
    }

    @Transactional
    public void updateDatabaseConfigs(String dbIdentifier) {
        // First update: set all other dbIdentifiers to 'isDefault = 0'
        databaseConfigRepository.updateDefaultsExcept(dbIdentifier);
        // Second update: set the specific dbIdentifier to 'isDefault = 1'
        databaseConfigRepository.updateSpecificDatabase(dbIdentifier);
    }
    
}
