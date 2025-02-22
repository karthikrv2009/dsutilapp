package com.datapig.repository;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.datapig.entity.DatabaseConfig;

@Repository
public interface DatabaseConfigRepository extends JpaRepository<DatabaseConfig, Long> {


    DatabaseConfig findByDbIdentifier(String dbIdentifier);

    @Modifying
    @Transactional
    @Query("UPDATE DatabaseConfig d SET d.defaultProfile = true WHERE d.dbIdentifier = :dbIdentifier")
    int updateSpecificDatabase(String dbIdentifier);

    @Modifying
    @Transactional
    @Query("UPDATE DatabaseConfig d SET d.defaultProfile = false WHERE d.dbIdentifier <> :dbIdentifier")
    int updateDefaultsExcept(String dbIdentifier);
}
