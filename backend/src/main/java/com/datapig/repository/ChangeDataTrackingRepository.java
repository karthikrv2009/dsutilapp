package com.datapig.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datapig.entity.ChangeDataTracking;



@Repository
public interface ChangeDataTrackingRepository   extends JpaRepository<ChangeDataTracking, Long> {

    List<ChangeDataTracking> findByTableNameAndDbIdentifier(String tableName,String dbIdentifier);
    
}
