package com.datapig.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Table;

import java.time.LocalDateTime;

import jakarta.persistence.Column;

@Entity
@Table(name = "Pipeline")
public class Pipeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pipelineid", columnDefinition = "uniqueidentifier", nullable = false)
    private String pipelineid;

    @Column(name = "foldername", length = 50)
    private String folderName;

    @Column(name = "pipelineStartTime")
    private LocalDateTime pipelineStartTime;

    @Column(name = "pipelineEndTime")
    private LocalDateTime pipelineEndTime;

    @Column(name = "staus")
    private int status;

    @Column(name = "db_identifier", nullable = false, length = 50)
    private String dbIdentifier;

    public String getDbIdentifier() {
        return dbIdentifier;
    }

    public void setDbIdentifier(String dbIdentifier) {
        this.dbIdentifier = dbIdentifier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPipelineid() {
        return pipelineid;
    }

    public void setPipelineid(String pipelineid) {
        this.pipelineid = pipelineid;
    }

    public LocalDateTime getPipelineStartTime() {
        return pipelineStartTime;
    }

    public void setPipelineStartTime(LocalDateTime pipelineStartTime) {
        this.pipelineStartTime = pipelineStartTime;
    }

    public LocalDateTime getPipelineEndTime() {
        return pipelineEndTime;
    }

    public void setPipelineEndTime(LocalDateTime pipelineEndTime) {
        this.pipelineEndTime = pipelineEndTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

}
