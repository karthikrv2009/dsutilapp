package com.datapig.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datapig.entity.ChangeDataTrackingPointer;

@Repository
public interface ChangeDataTrackingPointerRepository  extends JpaRepository<ChangeDataTrackingPointer, Long> {

    List<ChangeDataTrackingPointer> findByCdcTableNameAndDbIdentifier(String cdcTableName,String dbIdentifier);

}
