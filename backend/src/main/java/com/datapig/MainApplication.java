package com.datapig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.datapig.entity.IntialLoad;
import com.datapig.service.ALDSMetaDataPointerLoadService;
import com.datapig.service.AzureQueueListenerService;
import com.datapig.service.InitialLoadService;
import com.datapig.service.ParseModelJson;
import com.datapig.utility.ModelJsonDownloader;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
@EntityScan(basePackages = "com.datapig.entity") // Adjust this if needed
@EnableJpaRepositories(basePackages = "com.datapig.repository") // Adjust if needed
@ComponentScan({ "com.datapig.*" })
public class MainApplication implements CommandLineRunner {

    @Autowired
    private InitialLoadService initialLoadService;
    
    @Autowired
    private ParseModelJson parseModelJson;

    @Autowired
    private ALDSMetaDataPointerLoadService aldsMetaDataPointerLoadService;

    @Autowired
    private AzureQueueListenerService azureQueueListenerService;
    
    @Autowired
    private ModelJsonDownloader modelJsonDownloader;

    @Override
    public void run(String... args) {
    
        
        IntialLoad initialLoad=initialLoadService.getIntialLoad("DataPig");
        if(initialLoad==null){
        if(modelJsonDownloader.downloadFile()){
            parseModelJson.parseModelJson();
            initialLoad=new IntialLoad();
            initialLoad.setName("DBSynctUtilInitalLoad");
            initialLoad.setStatus(0);
            initialLoadService.save(initialLoad);
        }
       }

        if(initialLoad!=null){
            if((initialLoad.getStatus() == 0) || (initialLoad.getStatus() == 1) ){
                aldsMetaDataPointerLoadService.load();    
            }
        }
        initialLoad=initialLoadService.getIntialLoad("DBSynctUtilInitalLoad");
        if(initialLoad.getStatus()==2){
            azureQueueListenerService.startQueueListener();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
