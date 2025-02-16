package com.datapig.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.entity.ChangeDataTracking;
import com.datapig.repository.ChangeDataTrackingRepository;

@Service
public class ChangeDataTrackingService {

    @Autowired
    ChangeDataTrackingRepository changeDataTrackingRepository;

    public List<ChangeDataTracking> findByTableNameAndDbIdentifier(String tableName, String dbIdentifier) {
        return changeDataTrackingRepository.findByTableNameAndDbIdentifier(tableName, dbIdentifier);
    }

    public List<ChangeDataTracking> findByDbIdentifier(String dbIdentifier) {
        return changeDataTrackingRepository.findByDbIdentifier(dbIdentifier);
    }

    public ChangeDataTracking save(ChangeDataTracking changeDataTracking) {
        return changeDataTrackingRepository.save(changeDataTracking);
    }

    public List<ChangeDataTracking> findByStageStatusAndDbIdentifier(int stageStatus, String dbIdentifier) {
        return changeDataTrackingRepository.findByStageStatusAndDbIdentifier(stageStatus, dbIdentifier);
    }

    public List<ChangeDataTracking> findByTableNameAndDbIdentifierAndStageStatus(String tableName, String dbIdentifier,
            int stageStatus) {
        return changeDataTrackingRepository.findByTableNameAndDbIdentifierAndStageStatus(tableName, dbIdentifier,
                stageStatus);
    }

    public ChangeDataTracking findByCdcTableNameAndDbIdentifier(String cdcTableName, String dbIdentifier) {
        return changeDataTrackingRepository.findByCdcTableNameAndDbIdentifier(cdcTableName, dbIdentifier);
    }

    public List<ChangeDataTracking> findAll() {
        return changeDataTrackingRepository.findAll();
    }

}
