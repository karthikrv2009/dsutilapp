package com.datapig.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datapig.entity.Pipeline;


@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

    public List<Pipeline> findByFolderName(String folderName);

    @Query("SELECT p FROM Pipeline p WHERE p.pipelineStartTime BETWEEN :startDate AND :endDate")
    List<Pipeline> findPipelinesWithinDateRange(@Param("startDate") LocalDateTime startDate,@Param("endDate") LocalDateTime endDate);

}
