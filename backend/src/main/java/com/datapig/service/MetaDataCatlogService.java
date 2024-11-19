package com.datapig.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.datapig.repository.MetaDataCatlogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

import com.datapig.entity.MetaDataCatlog;

@Service
public class MetaDataCatlogService {

    @Autowired
    private MetaDataCatlogRepository metaDataCatlogRepository;

    public List<MetaDataCatlog> findAll() {
        return metaDataCatlogRepository.findAll();
    }

    public MetaDataCatlog save(MetaDataCatlog metaDataCatlog) {
        return metaDataCatlogRepository.save(metaDataCatlog);
    }

    public MetaDataCatlog getmetaDataCatlogServiceBytableName(String tableName) {
        Optional<MetaDataCatlog> entityOptional = metaDataCatlogRepository.findBytableName(tableName);
        return entityOptional.orElse(null); // Return the entity or null if not found
    }

}
