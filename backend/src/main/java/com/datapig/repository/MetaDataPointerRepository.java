package com.datapig.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.datapig.entity.MetaDataPointer;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaDataPointerRepository extends JpaRepository<MetaDataPointer, String> {

    List<MetaDataPointer> findBystageStatus(Short stageStatus);
    MetaDataPointer findByfolderName(String folderName);
}
