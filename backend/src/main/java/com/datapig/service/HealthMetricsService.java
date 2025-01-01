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

    public HealthMetrics save(HealthMetrics healthMetrics) {
        return healthMetricsRepository.save(healthMetrics);
    }

    public List<HealthMetrics> findByFolderName(String folderName) {
        java.util.List<HealthMetrics> entityOptional = healthMetricsRepository.findByFolderName(folderName);
        return entityOptional;
    }

    public List<HealthMetrics> findbyPipelineId(String pipelineId) {
        java.util.List<HealthMetrics> entityOptional = healthMetricsRepository.findByPipelineId(pipelineId);
        return entityOptional;
    }

    public List<HealthMetrics> findbyPipelineIdAndDbIdentifer(String pipelineId, String dbIdentifier) {
        java.util.List<HealthMetrics> entityOptional = healthMetricsRepository
                .findByPipelineIdAndDbIdentifier(pipelineId, dbIdentifier);
        return entityOptional;
    }

}
