package com.datapig.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datapig.entity.ChangeDataTracking;
import com.datapig.entity.ChangeDataTrackingPointer;



@Repository
public interface ChangeDataTrackingRepository   extends JpaRepository<ChangeDataTracking, Long> {

    List<ChangeDataTrackingPointer> findByCdcTableNameAndDbIdentifier(String cdcTableName,String dbIdentifier);

    List<ChangeDataTracking> findByTableNameAndDbIdentifier(String tableName,String dbIdentifier);
    
    List<ChangeDataTracking> findByStageStatusAndDbIdentifier(int stageStatus,String dbIdentifier);

    List<ChangeDataTracking> findByTableNameAndDbIdentifierAndStageStatus(String tableName,String dbIdentifier,int stageStatus);
}
