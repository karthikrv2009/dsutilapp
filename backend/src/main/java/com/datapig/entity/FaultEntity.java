package com.datapig.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "FaultEntity")
public class FaultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tablename", nullable = false, length = 50)
    private String tableName;

    @Column(name = "db_identifier", nullable = false, length = 50)
    private String dbIdentifier;

    @Lob
    @Column(name = "error_msg", length = 50)
    private String errorMsg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDbIdentifier() {
        return dbIdentifier;
    }

    public void setDbIdentifier(String dbIdentifier) {
        this.dbIdentifier = dbIdentifier;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    

}
