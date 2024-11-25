package com.datapig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datapig.entity.HealthMetrics;

import java.util.List;

@Repository
public interface HealthMetricsRepository extends JpaRepository<HealthMetrics, Long>{
  public List<HealthMetrics> findbyfolder(String folder);
}
