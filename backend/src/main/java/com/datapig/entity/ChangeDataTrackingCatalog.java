package com.datapig.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ChangeDataTrackingCatalog")
public class ChangeDataTrackingCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "db_identifier", nullable = false, length = 50)
    private String dbIdentifier;

    
    @Column(name = "cdc_table_name", nullable = false, length = 50)
    private String cdcTableName;

    @Column(name = "select_column", columnDefinition = "TEXT")
    private String selectColumn;

    @Column(name = "data_frame", columnDefinition = "TEXT")
    private String dataFrame;

    @Column(name = "column_names", columnDefinition = "TEXT")
    private String columnNames;

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

    public String getCdcTableName() {
        return cdcTableName;
    }

    public void setCdcTableName(String cdcTableName) {
        this.cdcTableName = cdcTableName;
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

}
