package com.datapig.service.dto;

public class TableData {
    private String tableName;
    private String lastSuccessfulRuntime;
    private int recordsImpacted;

    // Constructor
    public TableData(String tableName, String lastSuccessfulRuntime, int recordsImpacted) {
        this.tableName = tableName;
        this.lastSuccessfulRuntime = lastSuccessfulRuntime;
        this.recordsImpacted = recordsImpacted;
    }

    // Getters and Setters
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getLastSuccessfulRuntime() {
        return lastSuccessfulRuntime;
    }

    public void setLastSuccessfulRuntime(String lastSuccessfulRuntime) {
        this.lastSuccessfulRuntime = lastSuccessfulRuntime;
    }

    public int getRecordsImpacted() {
        return recordsImpacted;
    }

    public void setRecordsImpacted(int recordsImpacted) {
        this.recordsImpacted = recordsImpacted;
    }
}
