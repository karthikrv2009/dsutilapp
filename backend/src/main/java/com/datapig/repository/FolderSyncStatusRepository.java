package com.datapig.repository;

import com.datapig.entity.FolderSyncStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderSyncStatusRepository extends JpaRepository<FolderSyncStatus, Long> {

List<FolderSyncStatus> findByfolder(String folder);

List<FolderSyncStatus> findBycopyStatus(Short copyStatus);

@Query("SELECT COUNT(f) FROM FolderSyncStatus f WHERE f.copyStatus = :copyStatus")
long countByCopyStatus(@Param("copyStatus") Short copyStatus);
}

