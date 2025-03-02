package com.datapig;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;

import com.datapig.service.ALDSMetaDataPointerLoadService;
import com.datapig.service.AzureQueueListenerService;
import com.datapig.service.DatabaseConfigService;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.IntialLoad;
import com.datapig.service.InitialLoadService;

import java.util.List;

@SpringBootApplication
@EnableScheduling
@ComponentScan({ "com.datapig.*" })
public class MainApplication implements CommandLineRunner, Daemon {

    private Thread appThread;

    @Autowired
    DatabaseConfigService databaseConfigService;

    @Autowired
    AzureQueueListenerService azureQueueListenerService;

    @Autowired
    InitialLoadService initialLoadService;

    @Autowired
    ALDSMetaDataPointerLoadService aldsMetaDataPointerLoadService;

    @Override
    public void init(DaemonContext context) throws Exception {
        System.out.println("Initializing daemon...");
    }

    @Override
    public void start() throws Exception {
        appThread = new Thread(() -> {
            System.out.println("Starting the application...");
            SpringApplication.run(MainApplication.class);
        });
        appThread.start();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Stopping the application...");
    
        // Gracefully stop the queue listeners before shutdown
        if (azureQueueListenerService != null) {
            azureQueueListenerService.stopQueueListener();  // Stop queue listeners and wait for completion
        }
    
        // Optionally, join the application thread to ensure all listeners are done
        if (appThread != null && appThread.isAlive()) {
            appThread.join();  // Wait for the app thread to complete
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("Destroying daemon...");
    }

    @Override
    public void run(String... args) throws Exception {
        List<DatabaseConfig> databaseConfigs = databaseConfigService.getAllDatabaseConfigs();
        for (DatabaseConfig databaseConfig : databaseConfigs) {
            IntialLoad initialLoad = initialLoadService.getIntialLoad(databaseConfig.getDbIdentifier());
            if (initialLoad != null) {
                if (initialLoad.getQueueListenerStatus() == 1) {
                    azureQueueListenerService.startQueueListener(databaseConfig.getDbIdentifier());
                } else {
                    aldsMetaDataPointerLoadService.load(initialLoad.getDbIdentifier());
                }
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
