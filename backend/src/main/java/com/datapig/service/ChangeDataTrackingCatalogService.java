package com.datapig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.entity.ChangeDataTrackingCatalog;
import com.datapig.repository.ChangeDataTrackingCatalogRepository;

@Service
public class ChangeDataTrackingCatalogService {

    @Autowired
    ChangeDataTrackingCatalogRepository changeDataTrackingCatalogRepository;
    
    public ChangeDataTrackingCatalog save(ChangeDataTrackingCatalog changeDataTrackingCatalog){
        return changeDataTrackingCatalogRepository.save(changeDataTrackingCatalog);
    }
    public ChangeDataTrackingCatalog findbyCdcTableNameAndDbIdentifier(String cdcTableName,String dbIdentifier){
        return changeDataTrackingCatalogRepository.findbyCdcTableNameAndDbIdentifier(cdcTableName, dbIdentifier);
    }

}
