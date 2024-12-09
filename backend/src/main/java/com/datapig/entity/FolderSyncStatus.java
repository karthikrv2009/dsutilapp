package com.datapig.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "FolderSyncStatus")
public class FolderSyncStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Added ID to uniquely identify rows

    @Column(name = "folder", length = 50)
    private String folder;

    @Column(name = "tablename", length = 50)
    private String tableName;
 
    @Column(name = "copystatus")
    private Short copyStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Short getCopyStatus() {
        return copyStatus;
    }

    public void setCopyStatus(Short copyStatus) {
        this.copyStatus = copyStatus;
    }

    // Getters and Setters
}

