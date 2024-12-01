package com.datapig.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public Set<String> getAllTableName(){
        List<MetaDataCatlog> metaDataCatlogs=metaDataCatlogRepository.findAll();
        Set<String> tableNames=new HashSet<String>();
        for(MetaDataCatlog metaDataCatlog:metaDataCatlogs){
            tableNames.add(metaDataCatlog.getTableName());
        }
        return tableNames;
    }

    public MetaDataCatlog save(MetaDataCatlog metaDataCatlog) {
        return metaDataCatlogRepository.save(metaDataCatlog);
    }

    public MetaDataCatlog getmetaDataCatlogServiceBytableName(String tableName) {
        Optional<MetaDataCatlog> entityOptional = metaDataCatlogRepository.findBytableName(tableName);
        return entityOptional.orElse(null); // Return the entity or null if not found
    }

    public List<MetaDataCatlog> findBylastCopyStatus(short lastCopyStatus){
        List<MetaDataCatlog> metaDataCatlogs=metaDataCatlogRepository.findBylastCopyStatus(lastCopyStatus);
        return metaDataCatlogs;
    }

    public Integer getRowCount(String tableName) {
        return metaDataCatlogRepository.getRowCountByTableName(tableName);
    }

}
