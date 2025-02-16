package com.datapig.repository;

import com.datapig.entity.FolderSyncStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FolderSyncStatusRepository extends JpaRepository<FolderSyncStatus, Long> {

    List<FolderSyncStatus> findByfolder(String folder);

    List<FolderSyncStatus> findBycopyStatus(Short copyStatus);

    List<FolderSyncStatus> findBycopyStatusAndDbIdentifier(Short copyStatus, String dbIdentifier);

    @Query("SELECT f FROM FolderSyncStatus f " +
            "JOIN MetaDataPointer m ON m.folderName = f.folder AND m.dbIdentifier = f.dbIdentifier " +
            "WHERE f.tableName = :tableName AND f.dbIdentifier = :dbIdentifier " +
            "AND m.adlscreationtimestamp BETWEEN :startTimestamp AND :endTimestamp")
    List<FolderSyncStatus> findFolderSyncStatusByTimestampRange(
            @Param("tableName") String tableName,
            @Param("dbIdentifier") String dbIdentifier,
            @Param("startTimestamp") LocalDateTime startTimestamp,
            @Param("endTimestamp") LocalDateTime endTimestamp);

    List<FolderSyncStatus> findByDbIdentifierAndFolderAndCopyStatusAndArchived(String dbIdentifier, String folder,
            Short copyStatus, int archived);

    List<FolderSyncStatus> findByDbIdentifierAndFolderAndArchived(String dbIdentifier, String folder, int archived);

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

    @Query("SELECT f FROM FolderSyncStatus f " +
            "INNER JOIN MetaDataPointer m ON f.folder = m.folderName AND f.dbIdentifier = m.dbIdentifier " +
            "WHERE f.dbIdentifier = :dbIdentifier " +
            "AND m.adlsCreationTimestamp > :thresholdDate")
    List<FolderSyncStatus> findFoldersAfterThreshold(@Param("dbIdentifier") String dbIdentifier,
            @Param("thresholdDate") LocalDateTime thresholdDate);

}
