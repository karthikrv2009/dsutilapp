package com.datapig.repository;

import com.datapig.entity.FolderSyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderSyncStatusRepository extends JpaRepository<FolderSyncStatus, Long> {
}

