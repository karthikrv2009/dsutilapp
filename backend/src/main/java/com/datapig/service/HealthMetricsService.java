package com.datapig.service;

import org.springframework.stereotype.Service;

import com.datapig.entity.HealthMetrics;
import com.datapig.repository.HealthMetricsRepository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class HealthMetricsService {

    @Autowired
    HealthMetricsRepository healthMetricsRepository;

    public HealthMetrics save(HealthMetrics healthMetrics){
        return healthMetricsRepository.save(healthMetrics);
    }

    public List<HealthMetrics> findbyfolder(String folder){
        java.util.List<HealthMetrics> entityOptional = healthMetricsRepository.findbyfolder(folder);
        return entityOptional; 
    }
}
