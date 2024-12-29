package com.datapig.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "HealthMetrics")
public class HealthMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Added ID to uniquely identify rows

    @Column(name = "db_identifier", nullable = false, length = 50)
    private String dbIdentifier;

    @Column(name = "pipelineid", columnDefinition = "uniqueidentifier", nullable = false)
    private String pipelineId;

    @Column(name = "foldername", length = 50)
    private String folderName;

    public String getDbIdentifier() {
        return dbIdentifier;
    }

    public void setDbIdentifier(String dbIdentifier) {
        this.dbIdentifier = dbIdentifier;
    }

    @Column(name = "tablename", length = 50)
    private String tableName;

    @Column(name = "methodname", length = 50)
    private String methodname;

    @Column(name = "timespent")
    private Long timespent;

    @Column(name = "status")
    private int status;

    @Column(name = "rcount")
    private Long rcount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getMethodname() {
        return methodname;
    }

    public void setMethodname(String methodname) {
        this.methodname = methodname;
    }

    public long getTimespent() {
        return timespent;
    }

    public void setTimespent(long timespent) {
        this.timespent = timespent;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTimespent(Long timespent) {
        this.timespent = timespent;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getRcount() {
        return rcount;
    }

    public void setRcount(Long rcount) {
        this.rcount = rcount;
    }

}
