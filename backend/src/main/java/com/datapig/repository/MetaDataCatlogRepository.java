package com.datapig.repository;

import com.datapig.entity.MetaDataCatlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaDataCatlogRepository extends JpaRepository<MetaDataCatlog, String> {

    Optional<MetaDataCatlog> findBytableName(String tableName);
    List<MetaDataCatlog> findBylastCopyStatus(short lastCopyStatus);
}

