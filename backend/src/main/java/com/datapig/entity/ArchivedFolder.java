package com.datapig.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ArchivedFolder")
public class ArchivedFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_name", length = 50, nullable = false)
    private String folderName;

    @Column(name = "db_identifier", nullable = false, length = 50)
    private String dbIdentifier;

    @Column(name = "stage_status", nullable = false)
    private int stageStatus;

    @Column(name = "adlsarchivetimestamp")
    private LocalDateTime adlsarchivetimestamp;


    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
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

    public LocalDateTime getAdlsarchivetimestamp() {
        return adlsarchivetimestamp;
    }

    public void setAdlsarchivetimestamp(LocalDateTime adlsarchivetimestamp) {
        this.adlsarchivetimestamp = adlsarchivetimestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
