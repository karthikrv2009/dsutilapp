package com.datapig.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datapig.entity.EnvironmentConfig;
import com.datapig.repository.EnvironmentConfigRepository;

@Service
public class EnvironmentConfigService {

    @Autowired
    private EnvironmentConfigRepository environmentConfigRepository;

    public EnvironmentConfig getEnvironmentConfig() {
        return environmentConfigRepository.findAll().get(0);
    }

    public EnvironmentConfig saveEnvironmentConfig(EnvironmentConfig environmentConfig) {
        return environmentConfigRepository.save(environmentConfig);
    }

    public void deleteEnvironmentConfig(Long id) {
        environmentConfigRepository.deleteById(id);
    }

    public List<EnvironmentConfig> getAllEnvironmentConfigs() {
        return environmentConfigRepository.findAll();
    }

    public void updateEnvironmentConfigs(String id, EnvironmentConfig environmentConfig) {
        EnvironmentConfig existingEnvironmentConfig = environmentConfigRepository.findByD365Environment(id);
        existingEnvironmentConfig.setD365Environment(existingEnvironmentConfig.getD365Environment());
        existingEnvironmentConfig.setD365EnvironmentUrl(existingEnvironmentConfig.getD365EnvironmentUrl());
        existingEnvironmentConfig.setMaxLength(environmentConfig.getMaxLength());
        existingEnvironmentConfig.setStringOffSet(environmentConfig.getStringOffSet());
        existingEnvironmentConfig.setStringOutlierPath(environmentConfig.getStringOutlierPath());
        environmentConfigRepository.save(existingEnvironmentConfig);
    }
}
