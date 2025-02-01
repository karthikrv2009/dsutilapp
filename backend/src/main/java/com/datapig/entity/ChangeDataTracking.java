package com.datapig.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ChangeDataTracking")
public class ChangeDataTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "db_identifier", nullable = false, length = 50)
    private String dbIdentifier;

    @Column(name = "stage_status", nullable = false)
    private int stageStatus;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "cdc_table_name")
    private String cdcTableName;

    @Column(name = "adls_start_time")
    private LocalDateTime adlsStartTime;

    @Column(name = "adls_end_time")
    private LocalDateTime adlsEndTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDbIdentifier() {
        return dbIdentifier;
    }

    public void setDbIdentifier(String dbIdentifier) {
        this.dbIdentifier = dbIdentifier;
    }

    public int getStageStatus() {
        return stageStatus;
    }

    public void setStageStatus(int stageStatus) {
        this.stageStatus = stageStatus;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCdcTableName() {
        return cdcTableName;
    }

    public void setCdcTableName(String cdcTableName) {
        this.cdcTableName = cdcTableName;
    }

    public LocalDateTime getAdlsStartTime() {
        return adlsStartTime;
    }

    public void setAdlsStartTime(LocalDateTime adlsStartTime) {
        this.adlsStartTime = adlsStartTime;
    }

    public LocalDateTime getAdlsEndTime() {
        return adlsEndTime;
    }

    public void setAdlsEndTime(LocalDateTime adlsEndTime) {
        this.adlsEndTime = adlsEndTime;
    }

}
