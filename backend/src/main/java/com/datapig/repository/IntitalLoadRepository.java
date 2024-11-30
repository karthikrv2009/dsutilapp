package com.datapig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datapig.entity.IntialLoad;


@Repository
public interface IntitalLoadRepository extends JpaRepository<IntialLoad, String> {
    IntialLoad findByName(String name);
}
