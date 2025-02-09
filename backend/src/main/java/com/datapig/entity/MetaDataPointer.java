package com.datapig.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "MetaDataPointer")
public class MetaDataPointer {

    @Id
    @Column(name = "folder_name", length = 50, nullable = false)
    private String folderName;

    @Column(name = "adlscreationtimestamp")
    private LocalDateTime adlscreationtimestamp;

    @Column(name = "stageStatus", nullable = false)
    private Short stageStatus;

    @Column(name = "db_identifier", nullable = false, length = 50)
    private String dbIdentifier;

    public String getDbIdentifier() {
        return dbIdentifier;
    }

    public void setDbIdentifier(String dbIdentifier) {
        this.dbIdentifier = dbIdentifier;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(String storageAccount) {
        this.storageAccount = storageAccount;
    }

    @Column(name = "Environment", length = 400)
    private String environment;

    @Column(name = "StorageAccount", length = 200)
    private String storageAccount;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public LocalDateTime getAdlscreationtimestamp() {
        return adlscreationtimestamp;
    }

    public void setAdlscreationtimestamp(LocalDateTime adlscreationtimestamp) {
        this.adlscreationtimestamp = adlscreationtimestamp;
    }

    public Short getStageStatus() {
        return stageStatus;
    }

    public void setStageStatus(Short stageStatus) {
        this.stageStatus = stageStatus;
    }

    @Column(name = "stagestarttime")
    private LocalDateTime stageStartTime;

    @Column(name = "stageendtime")
    private LocalDateTime stageEndTime;

    public LocalDateTime getStageStartTime() {
        return stageStartTime;
    }

    public void setStageStartTime(LocalDateTime stageStartTime) {
        this.stageStartTime = stageStartTime;
    }

    public LocalDateTime getStageEndTime() {
        return stageEndTime;
    }

    public void setStageEndTime(LocalDateTime stageEndTime) {
        this.stageEndTime = stageEndTime;
    }

}
