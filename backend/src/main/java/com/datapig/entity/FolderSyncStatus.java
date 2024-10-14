package com.datapig.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.util.List;
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

    // Getters and Setters
}

