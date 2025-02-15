package com.datapig.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.datapig.entity.FaultEntity;


@Repository
public interface FaultEntityRepository extends JpaRepository<FaultEntity, Long> {

  @Query("SELECT f FROM FaultEntity f WHERE f.tableName = :tablename AND f.dbIdentifier = :dbIdentifier")
  List<FaultEntity> findByTableNameAndDbIdentifier(String tablename, String dbIdentifier);

}
