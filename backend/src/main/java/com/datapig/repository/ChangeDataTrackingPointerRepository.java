package com.datapig.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datapig.entity.ChangeDataTrackingPointer;

@Repository
public interface ChangeDataTrackingPointerRepository  extends JpaRepository<ChangeDataTrackingPointer, Long> {

    List<ChangeDataTrackingPointer> findByCdcTableNameAndDbIdentifier(String cdcTableName,String dbIdentifier);

    List<ChangeDataTrackingPointer>  findByCdcTableNameAndDbIdentifierAndRehydrationStatus(String cdcTableName,String dbIdentifier,int status);

    List<ChangeDataTrackingPointer>  findByDbIdentifierAndStageStatus(String dbIdentifier,int stageStatus);

    List<ChangeDataTrackingPointer>  findByDbIdentifierAndFolderName(String dbIdentifier,String folderName);

}
