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

    public List<ChangeDataTracking> findByTableNameAndDbIdentifier(String tableName, String dbIdentifier){
        return changeDataTrackingRepository.findByTableNameAndDbIdentifier(tableName, dbIdentifier);
    }

    public ChangeDataTracking save(ChangeDataTracking changeDataTracking){
        return changeDataTrackingRepository.save(changeDataTracking);
    }
}
