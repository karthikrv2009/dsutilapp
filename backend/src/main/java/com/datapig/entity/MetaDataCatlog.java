package com.datapig.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "MetaDataCatlog")
public class MetaDataCatlog {

    @Id
    @Column(name = "TableName", nullable = false, length = 50)
    private String tableName;

    @Column(name = "Environment", length = 100)
    private String environment;

    @Column(name = "StorageAccount", length = 100)
    private String storageAccount;

    @Column(name = "SelectColumn", columnDefinition = "TEXT")
    private String selectColumn;

    @Column(name = "DataFrame", columnDefinition = "TEXT")
    private String dataFrame;

    @Column(name = "column_names", columnDefinition = "TEXT")
    private String columnNames;

    @Column(name = "LastUpdatedFolder", length = 50)
    private String lastUpdatedFolder;

    @Column(name = "LastCopyStatus")
    private Boolean lastCopyStatus;

    @Column(name = "LastStartCopyDate")
    private LocalDateTime lastStartCopyDate;

    @Column(name = "LastEndCopyDate")
    private LocalDateTime lastEndCopyDate;

    // Getters and Setters
}
