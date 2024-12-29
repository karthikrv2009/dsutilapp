package com.datapig.service;

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

}
