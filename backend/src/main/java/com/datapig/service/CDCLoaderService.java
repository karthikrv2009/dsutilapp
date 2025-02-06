package com.datapig.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.entity.ChangeDataTracking;
import com.datapig.entity.ChangeDataTrackingPointer;

@Service
public class CDCLoaderService {

    @Autowired
    private ChangeDataTrackingService changeDataTrackingService;

    @Autowired
    private ChangeDataTrackingPointerService changeDataTrackingPointerService;

    @Autowired
    private FolderSyncStatusService folderSyncStatusService;

    public void loadCDC(String dbIdentifier,String tableName,LocalDateTime startTime,LocalDateTime endTime){
        ChangeDataTracking changeDataTracking=new ChangeDataTracking();
        String cdcTableName="cdc_"+tableName+LocalDateTime.now();
        changeDataTracking.setAdlsEndTime(endTime);
        changeDataTracking.setAdlsStartTime(startTime);
        changeDataTracking.setCdcTableName(cdcTableName);
        changeDataTracking.setDbIdentifier(dbIdentifier);
        changeDataTracking.setTableName(tableName);
        changeDataTracking.setStageStatus(0);
        changeDataTracking = changeDataTrackingService.save(changeDataTracking);
        
    }

    private ChangeDataTrackingPointer loadCDCPointer(ChangeDataTracking changeDataTracking){
        //sfolderSyncStatusService.findFolderSyncStatusByTimestampRange

        return null;
    }

}
