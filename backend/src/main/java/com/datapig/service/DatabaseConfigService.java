package com.datapig.service;

import java.util.List;

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
        return databaseConfigRepository.findByDbIdentifier(dbIdentifier);
    }

    public void saveDatabaseConfig(DatabaseConfig databaseConfig) {
        databaseConfigRepository.save(databaseConfig);
        dynamicDataSourceManager.addDataSource(databaseConfig.getDbIdentifier(), databaseConfig.getUrl(),
                databaseConfig.getUsername(), databaseConfig.getPassword());

    }

    public void deleteDatabaseConfig(DatabaseConfig databaseConfig) {
        databaseConfigRepository.deleteById(databaseConfig.getId());
        dynamicDataSourceManager.removeDataSource(databaseConfig.getDbIdentifier());
    }
}
