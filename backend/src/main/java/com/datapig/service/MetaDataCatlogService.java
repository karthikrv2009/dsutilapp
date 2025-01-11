package com.datapig.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

import com.datapig.repository.MetaDataCatlogRepository;
import com.datapig.utility.JDBCTemplateUtiltiy;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

import com.datapig.entity.MetaDataCatlog;

@Service
public class MetaDataCatlogService {

    @Autowired
    private JDBCTemplateUtiltiy jdbcTemplateUtiltiy;
    @Autowired
    private MetaDataCatlogRepository metaDataCatlogRepository;

    public List<MetaDataCatlog> findAll() {
        return metaDataCatlogRepository.findAll();
    }

    public Set<String> getAllTableName() {
        List<MetaDataCatlog> metaDataCatlogs = metaDataCatlogRepository.findAll();
        Set<String> tableNames = new HashSet<String>();
        for (MetaDataCatlog metaDataCatlog : metaDataCatlogs) {
            tableNames.add(metaDataCatlog.getTableName());
        }
        return tableNames;
    }

    public List<MetaDataCatlog> findAllByDbIdentifier(String dbIdentifier) {
        return metaDataCatlogRepository.findByDbIdentifier(dbIdentifier);
    }

    public Set<String> getAllTableNamesByDbIdentifier(String dbIdentifier) {
        List<MetaDataCatlog> metaDataCatlogs = metaDataCatlogRepository.findByDbIdentifier(dbIdentifier);
        Set<String> tableNames = new HashSet<String>();
        for (MetaDataCatlog metaDataCatlog : metaDataCatlogs) {
            tableNames.add(metaDataCatlog.getTableName());
        }
        return tableNames;
    }

    public MetaDataCatlog save(MetaDataCatlog metaDataCatlog) {
        return metaDataCatlogRepository.save(metaDataCatlog);
    }

    public MetaDataCatlog getMetaDataCatlogByTableNameAndDbIdentifier(String tableName, String dbIdentifier) {
        Optional<MetaDataCatlog> entityOptional = metaDataCatlogRepository.findByTableNameAndDbIdentifier(tableName,
                dbIdentifier);
        return entityOptional.orElse(null); // Return the entity or null if not found
    }

    public List<MetaDataCatlog> findBylastCopyStatus(short lastCopyStatus) {
        List<MetaDataCatlog> metaDataCatlogs = metaDataCatlogRepository.findBylastCopyStatus(lastCopyStatus);
        return metaDataCatlogs;
    }

    public Integer getRowCount(String tableName,String dbIdentifier) {
        return jdbcTemplateUtiltiy.getRowCountByTableName(tableName,dbIdentifier);
    }

    public List<MetaDataCatlog> findBylastCopyStatusAndDbIdentifier(short lastCopyStatus, String dbIdentifier) {
        return metaDataCatlogRepository.findBylastCopyStatusAndDbIdentifier(lastCopyStatus, dbIdentifier);
    }

}
