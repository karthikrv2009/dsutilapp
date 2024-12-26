package com.datapig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datapig.entity.DatabaseConfig;

@Repository
public interface DatabaseConfigRepository extends JpaRepository<DatabaseConfig, Long> {

    // Custom query methods (if needed)
    DatabaseConfig findByDbIdentifier(String dbIdentifier);
}
