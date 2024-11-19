package com.datapig.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



import java.time.LocalDateTime;

@Entity
@Table(name = "MetaDataCatlog")
public class MetaDataCatlog {

    @Id
    @Column(name = "TableName", nullable = false, length = 50)
    private String tableName;

    @Column(name = "SelectColumn", columnDefinition = "TEXT")
    private String selectColumn;

    @Column(name = "DataFrame", columnDefinition = "TEXT")
    private String dataFrame;

    @Column(name = "column_names", columnDefinition = "TEXT")
    private String columnNames;

    @Column(name = "LastUpdatedFolder", length = 50)
    private String lastUpdatedFolder;

    @Column(name = "LastCopyStatus")
    private short lastCopyStatus;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSelectColumn() {
        return selectColumn;
    }

    public void setSelectColumn(String selectColumn) {
        this.selectColumn = selectColumn;
    }

    public String getDataFrame() {
        return dataFrame;
    }

    public void setDataFrame(String dataFrame) {
        this.dataFrame = dataFrame;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public String getLastUpdatedFolder() {
        return lastUpdatedFolder;
    }

    public void setLastUpdatedFolder(String lastUpdatedFolder) {
        this.lastUpdatedFolder = lastUpdatedFolder;
    }



    public LocalDateTime getLastStartCopyDate() {
        return lastStartCopyDate;
    }

    public void setLastStartCopyDate(LocalDateTime lastStartCopyDate) {
        this.lastStartCopyDate = lastStartCopyDate;
    }

    public LocalDateTime getLastEndCopyDate() {
        return lastEndCopyDate;
    }

    public void setLastEndCopyDate(LocalDateTime lastEndCopyDate) {
        this.lastEndCopyDate = lastEndCopyDate;
    }


    @Column(name = "LastStartCopyDate")
    private LocalDateTime lastStartCopyDate;

    @Column(name = "LastEndCopyDate")
    private LocalDateTime lastEndCopyDate;

    public short getLastCopyStatus() {
        return lastCopyStatus;
    }

    public void setLastCopyStatus(short lastCopyStatus) {
        this.lastCopyStatus = lastCopyStatus;
    }

    // Getters and Setters
}
