package com.datapig.repository;

import com.datapig.entity.ConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository extends JpaRepository<ConfigurationEntity, Long> {
    // Custom query methods (if needed)
}
