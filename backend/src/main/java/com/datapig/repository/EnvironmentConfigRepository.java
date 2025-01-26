package com.datapig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datapig.entity.EnvironmentConfig;

@Repository
public interface EnvironmentConfigRepository extends JpaRepository<EnvironmentConfig, Long> {

    EnvironmentConfig findByD365Environment(String d365Environment);
}