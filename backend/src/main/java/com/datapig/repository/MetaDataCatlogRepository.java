package com.datapig.repository;

import com.datapig.entity.MetaDataCatlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaDataCatlogRepository extends JpaRepository<MetaDataCatlog, String> {

        List<MetaDataCatlog> findBylastCopyStatus(short lastCopyStatus);

        List<MetaDataCatlog> findByQuarintineAndDbIdentifier(int quarintine,String dbIdentifier);

        List<MetaDataCatlog> findBylastCopyStatusAndDbIdentifier(@Param("lastCopyStatus") short lastCopyStatus,
                        @Param("dbIdentifier") String dbIdentifier);

        List<MetaDataCatlog> findByDbIdentifier(@Param("dbIdentifier") String dbIdentifier);

        Optional<MetaDataCatlog> findByTableNameAndDbIdentifier(@Param("tableName") String tableName,
                        @Param("dbIdentifier") String dbIdentifier);

}
