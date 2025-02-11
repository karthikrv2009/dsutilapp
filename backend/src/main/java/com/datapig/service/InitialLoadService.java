package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.IntialLoad;
import com.datapig.repository.IntitalLoadRepository;
import com.datapig.utility.ModelJsonDownloader;

@Service
public class InitialLoadService {
    

    @Autowired
    IntitalLoadRepository intitalLoadRepository;

    @Autowired
    private ParseModelJson parseModelJson;

    @Autowired
    private ALDSMetaDataPointerLoadService aldsMetaDataPointerLoadService;

    @Autowired
    private AzureQueueListenerService azureQueueListenerService;

    @Autowired
    private ModelJsonDownloader modelJsonDownloader;

    @Autowired
    DatabaseConfigService databaseConfigService;

    public IntialLoad getIntialLoad(String dbIdentifier) {
        IntialLoad initialLoad = intitalLoadRepository.findByName(dbIdentifier);
        return initialLoad;
    }

    public IntialLoad save(IntialLoad initialLoad) {
        return intitalLoadRepository.save(initialLoad);
    }

    public void runInitialLoad(String dbIdentifier) {
        DatabaseConfig databaseConfig = databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
        IntialLoad initialLoad = intitalLoadRepository.findByName(dbIdentifier);
        if (initialLoad == null) {
            if (modelJsonDownloader.downloadFile(databaseConfig.getDbIdentifier())) {
                boolean forCDC=false;
                parseModelJson.parseModelJson(dbIdentifier,forCDC);
                initialLoad = new IntialLoad();
                initialLoad.setName(dbIdentifier);
                initialLoad.setStatus(0);
                initialLoad.setDbIdentifier(dbIdentifier);
                intitalLoadRepository.save(initialLoad);
            }
        }

        if (initialLoad != null) {
            if ((initialLoad.getStatus() == 0) || (initialLoad.getStatus() == 1)) {
                aldsMetaDataPointerLoadService.load(dbIdentifier);
            }
        }

        
         initialLoad = intitalLoadRepository.findByName(dbIdentifier);
         if (initialLoad.getStatus() == 2) {
            initialLoad.setQueueListenerStatus(1);
            intitalLoadRepository.save(initialLoad);
          azureQueueListenerService.startQueueListener(databaseConfig.getDbIdentifier());
          }
         
    }
}
