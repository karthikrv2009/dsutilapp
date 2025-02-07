package com.datapig.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ChangeDataTrackingCatalog")
public class ChangeDataTrackingPointer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "db_identifier", nullable = false, length = 50)
    private String dbIdentifier;

    
    @Column(name = "cdc_table_name", nullable = false, length = 50)
    private String cdcTableName;

    @Column(name="folder_name", nullable = false, length = 50)
    private String folderName;

    //0=not started,1=rehydration started,2=rehydration completed
    @Column(name="rehydration_status")
    private int rehydrationStatus;

    //0=not started,1= success,2 =fail
    @Column(name="stage_status")
    private int stageStatus;

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

    public String getCdcTableName() {
        return cdcTableName;
    }

    public void setCdcTableName(String cdcTableName) {
        this.cdcTableName = cdcTableName;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getRehydrationStatus() {
        return rehydrationStatus;
    }

    public void setRehydrationStatus(int rehydrationStatus) {
        this.rehydrationStatus = rehydrationStatus;
    }

}
