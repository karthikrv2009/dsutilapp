package com.datapig.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.datapig.entity.ArchivedFolder;

@Repository
public interface ArchivedFolderRepository extends JpaRepository<ArchivedFolder, Long> {

  @Query("SELECT a FROM ArchivedFolder a WHERE a.stageStatus = :stageStatus AND a.dbIdentifier = :dbIdentifier ORDER BY a.adlsarchivetimestamp")
  List<ArchivedFolder> findByStageStatusAndDbIdentifier(int stageStatus, String dbIdentifier);
  
  ArchivedFolder findByFolderNameAndDbIdentifier(String folderName,String dbIdentifier);
}
