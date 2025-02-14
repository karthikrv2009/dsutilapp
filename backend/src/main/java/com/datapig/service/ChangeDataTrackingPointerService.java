package com.datapig.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.entity.ChangeDataTrackingPointer;
import com.datapig.repository.ChangeDataTrackingPointerRepository;

@Service
public class ChangeDataTrackingPointerService {

    @Autowired
    ChangeDataTrackingPointerRepository changeDataTrackingPointerRepository;

    public int readyToArchive(String dbIdentifier,String folderName){
        return changeDataTrackingPointerRepository.readyToArchive(dbIdentifier,folderName);
    }
    public List<ChangeDataTrackingPointer>  findByDbIdentifierAndStageStatus(String dbIdentifier,int stageStatus){
        return changeDataTrackingPointerRepository.findByDbIdentifierAndStageStatus(dbIdentifier, stageStatus);
    }
    
    public List<ChangeDataTrackingPointer> findByCdcTableNameAndDbIdentifier(String cdcTableName,String dbIdentifier){
        return changeDataTrackingPointerRepository.findByCdcTableNameAndDbIdentifier(cdcTableName, dbIdentifier);
    }

    public List<ChangeDataTrackingPointer>  findByDbIdentifierAndFolderName(String dbIdentifier,String folderName){
        return changeDataTrackingPointerRepository.findByDbIdentifierAndFolderName(dbIdentifier, folderName);
    }


    public ChangeDataTrackingPointer save(ChangeDataTrackingPointer changeDataTrackingPointer){
        return changeDataTrackingPointerRepository.save(changeDataTrackingPointer);
    }

    public List<ChangeDataTrackingPointer>  findByCdcTableNameAndDbIdentifierAndRehydrationStatus(String cdcTableName,String dbIdentifier,int status){
        return changeDataTrackingPointerRepository.findByCdcTableNameAndDbIdentifierAndRehydrationStatus(cdcTableName, dbIdentifier, status);
    }

}
