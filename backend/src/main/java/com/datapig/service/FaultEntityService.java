package com.datapig.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.entity.FaultEntity;
import com.datapig.entity.HealthMetrics;
import com.datapig.repository.FaultEntityRepository;

@Service
public class FaultEntityService {

    @Autowired
    FaultEntityRepository faultEntityRepository;

    public FaultEntity save(FaultEntity faultEntity) {
        return faultEntityRepository.save(faultEntity);
    }

    public List<FaultEntity> findbytableNameAndDbIdentifer(String tableName, String dbIdentifier) {
        java.util.List<FaultEntity> entityOptional = faultEntityRepository
                .findByT(tableName, dbIdentifier);
        return entityOptional;
    }

}
