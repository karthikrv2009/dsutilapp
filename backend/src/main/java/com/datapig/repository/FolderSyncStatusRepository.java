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

        List<FolderSyncStatus> findBycopyStatusAndDbIdentifier(Short copyStatus, String dbIdentifier);

        @Query("SELECT COUNT(f) FROM FolderSyncStatus f WHERE f.copyStatus = :copyStatus")
        long countByCopyStatus(@Param("copyStatus") Short copyStatus);

        @Query("SELECT f FROM FolderSyncStatus f WHERE f.folder = :folder AND f.tableName = :tableName")
        FolderSyncStatus getFolderSyncStatusOnFolderAndTableName(@Param("folder") String folder,
                        @Param("tableName") String tableName);

        @Query("SELECT f FROM FolderSyncStatus f WHERE f.folder = :folder AND f.tableName = :tableName AND f.dbIdentifier = :dbIdentifier")
        FolderSyncStatus getFolderSyncStatusOnFolderAndTableNameAndDBIdentifier(@Param("folder") String folder,
                        @Param("tableName") String tableName, @Param("dbIdentifier") String dbIdentifier);

        List<FolderSyncStatus> findByfolderAndDbIdentifier(String folder, String dbIdentifier);

        List<FolderSyncStatus> findBycopyStatusAndDbIdentifierAndTableName(Short copyStatus, String dbIdentifier,
                        String tableName);

        @Query("SELECT COUNT(f) FROM FolderSyncStatus f WHERE f.copyStatus = :copyStatus AND f.dbIdentifier = :dbIdentifier")
        long countByCopyStatusAndDbIdentifier(@Param("copyStatus") Short copyStatus,
                        @Param("dbIdentifier") String dbIdentifier);
}
