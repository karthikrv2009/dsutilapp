package com.datapig.repository;

import com.datapig.entity.MetaDataCatlog;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MetaDataCatlogRepository extends JpaRepository<MetaDataCatlog, String> {
}

