package com.datapig.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "IntialLoad")
public class IntialLoad {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "starttimestamp")
    private LocalDateTime starttimestamp;

    @Column(name = "endtimestamp")
    private LocalDateTime endtimestamp;

    @Column(name = "status")
    private int status;

    @Column(name = "totalpackages")
    private long totalpackages;

    @Column(name = "stagestarttime")
    private LocalDateTime stagestarttime;

    @Column(name = "stageendtime")
    private LocalDateTime stageendtime;

    @Column(name = "db_identifier", nullable = false, length = 50)
    private String dbIdentifier;

    public void setDbIdentifier(String dbIdentifier) {
        this.dbIdentifier = dbIdentifier;
    }

    public String getDbIdentifier() {
        return dbIdentifier;
    }

    public LocalDateTime getStarttimestamp() {
        return starttimestamp;
    }

    public void setStarttimestamp(LocalDateTime starttimestamp) {
        this.starttimestamp = starttimestamp;
    }

    public LocalDateTime getEndtimestamp() {
        return endtimestamp;
    }

    public void setEndtimestamp(LocalDateTime endtimestamp) {
        this.endtimestamp = endtimestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getStagestarttime() {
        return stagestarttime;
    }

    public void setStagestarttime(LocalDateTime stagestarttime) {
        this.stagestarttime = stagestarttime;
    }

    public LocalDateTime getStageendtime() {
        return stageendtime;
    }

    public void setStageendtime(LocalDateTime stageendtime) {
        this.stageendtime = stageendtime;
    }

    @Column(name = "stagestatus")
    private int stagestatus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStagestatus() {
        return stagestatus;
    }

    public void setStagestatus(int stagestatus) {
        this.stagestatus = stagestatus;
    }

    public long getTotalpackages() {
        return totalpackages;
    }

    public void setTotalpackages(long totalpackages) {
        this.totalpackages = totalpackages;
    }

}
