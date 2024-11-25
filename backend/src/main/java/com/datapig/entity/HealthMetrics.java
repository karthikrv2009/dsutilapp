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

    @Column(name = "foldername", length = 50)
    private String foldername;

    @Column(name = "tablename", length = 50)
    private String tableName;

    @Column(name = "methodname", length = 50)
    private String methodname;

    @Column(name="timespent")
    private long timespent;

    @Column(name = "status")
    private int status;

    @Column (name="rowcount")
    private long rowcount;

    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFoldername() {
        return foldername;
    }

    public void setFoldername(String foldername) {
        this.foldername = foldername;
    }

    public String getTableName() {
        return tableName;
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

    public long getRowcount() {
        return rowcount;
    }

    public void setRowcount(long rowcount) {
        this.rowcount = rowcount;
    }


}
