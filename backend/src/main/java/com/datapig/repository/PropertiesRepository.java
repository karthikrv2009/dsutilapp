package com.datapig.repository;


import com.datapig.entity.ApplicationProperty;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertiesRepository extends JpaRepository<ApplicationProperty, Long> {

    Optional<ApplicationProperty> findByPropertyName(String propertyName);
}
