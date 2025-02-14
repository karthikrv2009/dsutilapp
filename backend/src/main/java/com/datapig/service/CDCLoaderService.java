package com.datapig.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.datapig.entity.ChangeDataTracking;
import com.datapig.entity.ChangeDataTrackingPointer;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.FolderSyncStatus;

import com.datapig.utility.ArchiveToHotRehydration;

@Service
public class CDCLoaderService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private ChangeDataTrackingService changeDataTrackingService;

    @Autowired
    private ChangeDataTrackingPointerService changeDataTrackingPointerService;

    @Autowired
    private FolderSyncStatusService folderSyncStatusService;

    @Autowired
    private ArchiveToHotRehydration archiveToHotRehydration;

    @Autowired
    private ParseModelJson parseModelJson;

    @Autowired
    DatabaseConfigService databaseConfigService;

    public ChangeDataTracking loadCDC(String dbIdentifier,String tableName,LocalDateTime startTime,LocalDateTime endTime){
        DatabaseConfig databaseConfig=databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
        System.out.println(dbIdentifier+"===>"+tableName+"===>"+startTime+"===>"+endTime);
        String containerName=databaseConfig.getAdlsContainerName();
        ChangeDataTracking changeDataTracking=new ChangeDataTracking();
        // Get the current time in milliseconds since the epoch
        long epochTime = System.currentTimeMillis();
        String cdcTableName="cdc_"+tableName+"_"+epochTime;
        changeDataTracking.setAdlsEndTime(endTime);
        changeDataTracking.setAdlsStartTime(startTime);
        changeDataTracking.setCdcTableName(cdcTableName);
        changeDataTracking.setDbIdentifier(dbIdentifier);
        changeDataTracking.setTableName(tableName);
        changeDataTracking.setStageStatus(0);
        
        List<ChangeDataTrackingPointer> lChangeDataTrackingPointers=loadCDCPointer(changeDataTracking);
        List<ChangeDataTrackingPointer> lChangeDataTrackingPointersInDB=new ArrayList<ChangeDataTrackingPointer>();
        for(ChangeDataTrackingPointer changeDataTrackingPointer:lChangeDataTrackingPointers){
            changeDataTrackingPointer=changeDataTrackingPointerService.save(changeDataTrackingPointer);
            lChangeDataTrackingPointersInDB.add(changeDataTrackingPointer);
        }
        if(lChangeDataTrackingPointersInDB!=null){
            if(lChangeDataTrackingPointersInDB.size()!=0){
                
            
            for(ChangeDataTrackingPointer changeDataTrackingPointer:lChangeDataTrackingPointersInDB){
                
                System.out.println(changeDataTrackingPointer.getFolderName());
                String path=null;

                if(changeDataTrackingPointer.getFolderName().contains("/model.json")){
                    path=changeDataTrackingPointer.getFolderName();
                    System.out.println(path);
                    boolean flag=archiveToHotRehydration.rehydrateBlobToHotTier(containerName, path,databaseConfig);
                    if(flag){
                        changeDataTrackingPointer=updateRehydrationToStart(changeDataTrackingPointer);
                        boolean modelflag=archiveToHotRehydration.checkRehydrationStatus(databaseConfig.getAdlsContainerName(), path,databaseConfig);
                        if(modelflag){
                            changeDataTrackingPointer=updateRehydrationToComplete(changeDataTrackingPointer);
                            parseModelJson.parseCdcModelJson(changeDataTracking.getDbIdentifier(),changeDataTracking.getCdcTableName(),changeDataTracking.getTableName());
                            updateStageStatusToComplete(changeDataTrackingPointer);
                        }
                    }
                }
                else{
                    
                    path=changeDataTrackingPointer.getFolderName()+"/"+changeDataTracking.getTableName();
                    System.out.println(path);
                    boolean flag=archiveToHotRehydration.rehydrateToHotTier(containerName, path,databaseConfig);
                    if(flag){
                       changeDataTrackingPointer=updateRehydrationToStart(changeDataTrackingPointer);
                    }
                }
            }
            }
        }
        else{
            changeDataTracking=null;
        }
        return changeDataTracking;
    }

    private ChangeDataTrackingPointer updateStageStatusToComplete(ChangeDataTrackingPointer changeDataTrackingPointer){
        //Rehydration Completed
        int stageStatus = 1;
        changeDataTrackingPointer.setRehydrationStatus(stageStatus);
        changeDataTrackingPointerService.save(changeDataTrackingPointer);
        return changeDataTrackingPointer;
    }

    private ChangeDataTrackingPointer updateRehydrationToComplete(ChangeDataTrackingPointer changeDataTrackingPointer){
        //Rehydration Completed
        int rehydrationStatus = 2;
        changeDataTrackingPointer.setRehydrationStatus(rehydrationStatus);
        changeDataTrackingPointer=changeDataTrackingPointerService.save(changeDataTrackingPointer);
        return changeDataTrackingPointer;
    }


    private ChangeDataTrackingPointer updateRehydrationToStart(ChangeDataTrackingPointer changeDataTrackingPointer){
        //Rehydration Started
        int rehydrationStatus = 1;
        changeDataTrackingPointer.setRehydrationStatus(rehydrationStatus);
        changeDataTrackingPointerService.save(changeDataTrackingPointer);
        return changeDataTrackingPointer;
    }

    private List<ChangeDataTrackingPointer> loadCDCPointer(ChangeDataTracking changeDataTracking){
        List<ChangeDataTrackingPointer> lChangeDataTrackingPointers=new ArrayList<ChangeDataTrackingPointer>();
        System.out.println("changeDataTracking===>"+changeDataTracking.getCdcTableName()+"===>"+changeDataTracking.getTableName());
        int i =0;
        if(changeDataTracking!=null){
            List<FolderSyncStatus> lstfolderSyncStatus =folderSyncStatusService.findFolderSyncStatusByTimestampRange(changeDataTracking.getTableName(), changeDataTracking.getDbIdentifier(), changeDataTracking.getAdlsStartTime(),changeDataTracking.getAdlsEndTime());
            for(FolderSyncStatus folderSyncStatus:lstfolderSyncStatus){
                System.out.println("Inside loadCDCPointer====>"+folderSyncStatus.getTableName() +"==>"+folderSyncStatus.getFolder());
                if(i==0){
                    ChangeDataTrackingPointer modelchangeDataTrackingPointer=new ChangeDataTrackingPointer();
                    modelchangeDataTrackingPointer.setCdcTableName(changeDataTracking.getCdcTableName());
                    modelchangeDataTrackingPointer.setDbIdentifier(folderSyncStatus.getDbIdentifier());
                    modelchangeDataTrackingPointer.setFolderName(folderSyncStatus.getFolder()+"/model.json");
                    modelchangeDataTrackingPointer.setRehydrationStatus(0);
                    modelchangeDataTrackingPointer.setStageStatus(0);
                    lChangeDataTrackingPointers.add(modelchangeDataTrackingPointer);
                    
                    ChangeDataTrackingPointer changeDataTrackingPointer=new ChangeDataTrackingPointer();
                    changeDataTrackingPointer.setCdcTableName(changeDataTracking.getCdcTableName());
                    changeDataTrackingPointer.setDbIdentifier(folderSyncStatus.getDbIdentifier());
                    changeDataTrackingPointer.setFolderName(folderSyncStatus.getFolder());
                    changeDataTrackingPointer.setRehydrationStatus(0);
                    changeDataTrackingPointer.setStageStatus(0);
                    lChangeDataTrackingPointers.add(changeDataTrackingPointer);
                }
                else{
                    ChangeDataTrackingPointer changeDataTrackingPointer=new ChangeDataTrackingPointer();
                    changeDataTrackingPointer.setCdcTableName(changeDataTracking.getCdcTableName());
                    changeDataTrackingPointer.setDbIdentifier(folderSyncStatus.getDbIdentifier());
                    changeDataTrackingPointer.setFolderName(folderSyncStatus.getFolder());
                    changeDataTrackingPointer.setRehydrationStatus(0);
                    changeDataTrackingPointer.setStageStatus(0);
                    lChangeDataTrackingPointers.add(changeDataTrackingPointer);
                }
                i=i+1;
            }
        }
        return lChangeDataTrackingPointers;
    }

    public boolean stageDataFromADLS(String dataSource, String folder, String tableName, String dataFrame,
            String selectColumn) {
        boolean flag=false;
        String query = "INSERT INTO dbo._staging_" + tableName +
                " SELECT " + selectColumn +
                " FROM OPENROWSET(BULK '/" + folder + "/" + tableName + "/*.csv', FORMAT = 'CSV', DATA_SOURCE = '"
                + dataSource + "',CODEPAGE='65001') " +
                "WITH (" + dataFrame + ") AS " + tableName;
        
        try {
            jdbcTemplate.update(query);
            flag=true;
        } catch (Exception e) {
            flag=false;
        }
        return flag;
    }

}
