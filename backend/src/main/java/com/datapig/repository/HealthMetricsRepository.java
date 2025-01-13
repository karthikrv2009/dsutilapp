package com.datapig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.datapig.entity.HealthMetrics;

import java.util.List;

@Repository
public interface HealthMetricsRepository extends JpaRepository<HealthMetrics, Long> {
  public List<HealthMetrics> findByFolderName(String folderName);

  List<HealthMetrics> findByPipelineId(String pipelineId);
  @Query("SELECT h FROM HealthMetrics h WHERE h.pipelineId = :pipelineId AND h.dbIdentifier = :dbIdentifier ORDER BY h.tableName,h.timespent")
  List<HealthMetrics> findByPipelineIdAndDbIdentifier(String pipelineId, String dbIdentifier);

}
