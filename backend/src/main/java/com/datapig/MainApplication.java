package com.datapig;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;

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

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Autowired
    private AzureQueueListenerService azureQueueListenerService;

    @Autowired
    private InitialLoadService initialLoadService;

    @Autowired
    private ALDSMetaDataPointerLoadService aldsMetaDataPointerLoadService;

    // Used to hold the Spring application thread reference
    private Thread appThread;

    @Override
    public void init(DaemonContext context) throws Exception {
        logger.info("Initializing daemon...");
    }

    @Override
    public void start() throws Exception {
        appThread = new Thread(() -> {
            logger.info("Starting the application...");
            SpringApplication.run(MainApplication.class);
        });
        appThread.start();
        
        // Wait for the application to start (this ensures prunsrv doesn't return early)
        while (!appThread.isAlive()) {
            Thread.sleep(100); // Sleep for a bit and then check again
        }
    }
    
    @PreDestroy
    @Override
    public void stop() throws Exception {
        logger.info("Stopping the application...");
    
        // Gracefully stop the queue listeners before shutdown
        if (azureQueueListenerService != null) {
            logger.info("Stopping Azure queue listener...");
            azureQueueListenerService.stopQueueListener(); // Ensure it stops gracefully
        }
    
        // Optionally, join the application thread to ensure all listeners are done
        if (appThread != null && appThread.isAlive()) {
            logger.info("Interrupting the application thread...");
            appThread.interrupt();  // Interrupt the thread to stop gracefully
            appThread.join(10000);  // Allow 10 seconds for the app thread to finish
            logger.info("Application thread stopped.");
        }
    
        logger.info("Application stopped.");
    }
    
    @Override
    public void destroy() {
        logger.info("Destroying daemon...");
    }

    @Override
    public void run(String... args) throws Exception {
        List<DatabaseConfig> databaseConfigs = databaseConfigService.getAllDatabaseConfigs();
        for (DatabaseConfig databaseConfig : databaseConfigs) {
            IntialLoad initialLoad = initialLoadService.getIntialLoad(databaseConfig.getDbIdentifier());
            if (initialLoad != null) {
                if (initialLoad.getQueueListenerStatus() == 1) {
                    azureQueueListenerService.startQueueListener(databaseConfig.getDbIdentifier());
                    logger.info("Started queue listener for DB: {}", databaseConfig.getDbIdentifier());
                } else {
                    aldsMetaDataPointerLoadService.load(initialLoad.getDbIdentifier());
                    logger.info("Loaded metadata pointer for DB: {}", initialLoad.getDbIdentifier());
                }
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
