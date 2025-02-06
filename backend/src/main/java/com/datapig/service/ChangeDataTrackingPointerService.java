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

    public List<ChangeDataTrackingPointer> findByCdcTableNameAndDbIdentifier(String cdcTableName,String dbIdentifier){
        return changeDataTrackingPointerRepository.findByCdcTableNameAndDbIdentifier(cdcTableName, dbIdentifier);
    }

}
