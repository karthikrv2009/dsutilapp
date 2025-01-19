package com.datapig.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.datapig.entity.DatabaseConfig;
import com.datapig.service.DatabaseConfigService;

import java.util.List;

@Component
public class DatabaseConfigScheduler {

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Scheduled(fixedRate = 300000) // 300000 milliseconds = 5 minutes
    public void processDatabaseConfigs() {
        List<DatabaseConfig> configs = databaseConfigService.getAllDatabaseConfigs();
        for (DatabaseConfig config : configs) {
            if (config.isEnableArchive()) {

                // Add your code here to archive the database
            }
        }
    }
}
