package com.datapig.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datapig.entity.ChangeDataTrackingPointer;

@Repository
public interface ChangeDataTrackingPointerRepository  extends JpaRepository<ChangeDataTrackingPointer, Long> {

    @Query("SELECT COUNT(p) FROM ChangeDataTrackingPointer p JOIN ChangeDataTracking c " +
    "ON c.cdcTableName = p.cdcTableName AND c.dbIdentifier = p.dbIdentifier " +
    "WHERE p.dbIdentifier = :dbIdentifier AND p.stageStatus = 1 AND c.stageStatus <> 2 AND p.folderName = :folderName")
    int readyToArchive(@Param("dbIdentifier") String dbIdentifier, @Param("folderName") String folderName);
 
    List<ChangeDataTrackingPointer> findByCdcTableNameAndDbIdentifier(String cdcTableName,String dbIdentifier);

    List<ChangeDataTrackingPointer>  findByCdcTableNameAndDbIdentifierAndRehydrationStatus(String cdcTableName,String dbIdentifier,int status);

    List<ChangeDataTrackingPointer>  findByDbIdentifierAndStageStatus(String dbIdentifier,int stageStatus);

    List<ChangeDataTrackingPointer>  findByDbIdentifierAndFolderName(String dbIdentifier,String folderName);

}
