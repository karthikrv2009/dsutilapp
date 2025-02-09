package com.datapig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datapig.entity.ChangeDataTrackingCatalog;

@Repository
public interface ChangeDataTrackingCatalogRepository extends JpaRepository<ChangeDataTrackingCatalog, Long> {

    ChangeDataTrackingCatalog findByCdcTableNameAndDbIdentifier(String cdcTableName,String dbIdentifier);

}
