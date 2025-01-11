package com.datapig.repository;

import com.datapig.entity.MetaDataCatlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaDataCatlogRepository extends JpaRepository<MetaDataCatlog, String> {

        List<MetaDataCatlog> findBylastCopyStatus(short lastCopyStatus);

        @Query(value = "SELECT SUM(p.rows) FROM sys.partitions p " +
                        "JOIN sys.tables t ON p.object_id = t.object_id " +
                        "WHERE t.name = :tableName AND t.is_ms_shipped = 0 AND p.index_id IN (0, 1)", nativeQuery = true)
        Integer getRowCountByTableName(@Param("tableName") String tableName);

        List<MetaDataCatlog> findBylastCopyStatusAndDbIdentifier(@Param("lastCopyStatus") short lastCopyStatus,
                        @Param("dbIdentifier") String dbIdentifier);

        List<MetaDataCatlog> findByDbIdentifier(@Param("dbIdentifier") String dbIdentifier);

        Optional<MetaDataCatlog> findByTableNameAndDbIdentifier(@Param("tableName") String tableName,
                        @Param("dbIdentifier") String dbIdentifier);

}
