package com.datapig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datapig.entity.MetaDataPointer;

import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MetaDataPointerRepository extends JpaRepository<MetaDataPointer, String> {

    List<MetaDataPointer> findBystageStatus(Short stageStatus);
    MetaDataPointer findByfolderName(String folderName);

    @Query("SELECT m FROM MetaDataPointer m WHERE m.adlsCreationTimestamp = (SELECT MAX(m2.adlsCreationTimestamp) FROM MetaDataPointer m2)")
    MetaDataPointer findMaxAdlsCreationTimestamp();

    @Query("SELECT m FROM MetaDataPointer m WHERE m.stageStatus = :stageStatus ORDER BY m.adlsCreationTimestamp ASC")
    MetaDataPointer findFirstByStageStatusOrderByAdlsCreationTimestampAsc(@Param("stageStatus") Short stageStatus);

    @Query("SELECT COUNT(m) FROM MetaDataPointer m WHERE m.stageStatus = :stageStatus")
    int countByStageStatus(@Param("stageStatus") Short stageStatus);    

}
