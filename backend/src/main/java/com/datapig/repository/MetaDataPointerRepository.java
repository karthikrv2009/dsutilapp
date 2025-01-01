package com.datapig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import com.datapig.entity.MetaDataPointer;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaDataPointerRepository extends JpaRepository<MetaDataPointer, String> {

    List<MetaDataPointer> findBystageStatus(Short stageStatus);

    List<MetaDataPointer> findBystageStatusAndDbIdentifier(Short stageStatus, String dbIdentifier);

    MetaDataPointer findByfolderName(String folderName);

    @Query("SELECT m FROM MetaDataPointer m WHERE m.adlscreationtimestamp = (SELECT MAX(m2.adlscreationtimestamp) FROM MetaDataPointer m2)")
    MetaDataPointer findMaxAdlsCreationTimestamp();

    @Query("SELECT m FROM MetaDataPointer m WHERE m.dbIdentifier = :dbIdentifier AND m.adlscreationtimestamp = (SELECT MAX(m2.adlscreationtimestamp) FROM MetaDataPointer m2 WHERE m2.dbIdentifier = :dbIdentifier)")
    MetaDataPointer findMaxAdlsCreationTimestampByDbIdentifier(@Param("dbIdentifier") String dbIdentifier);

    @Query("SELECT m FROM MetaDataPointer m WHERE m.stageStatus = :stageStatus ORDER BY m.adlscreationtimestamp ASC")
    List<MetaDataPointer> findByStageStatusOrderByAdlsCreationTimestampAsc(@Param("stageStatus") Short stageStatus,
            Pageable pageable);

    @Query("SELECT m FROM MetaDataPointer m WHERE m.stageStatus = :stageStatus AND m.dbIdentifier = :dbIdentifier ORDER BY m.adlscreationtimestamp ASC")
    List<MetaDataPointer> findBystageStatusAndDbIdentifierOrderByAdlsCreationTimestampAsc(
            @Param("stageStatus") Short stageStatus, @Param("dbIdentifier") String dbIdentifier);

    long countByStageStatusAndDbIdentifier(Short stageStatus, String dbIdentifier);

    MetaDataPointer findBydbIdentifierAndFolderName(String dbIdentifier, String folderName);

    int countByStageStatus(Short stageStatus);
}
