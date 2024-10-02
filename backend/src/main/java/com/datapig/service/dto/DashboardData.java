package com.datapig.service.dto; 

import java.util.List;

public class DashboardData {
    private String lastSuccessfulRuntime;
    private int totalTablesImpacted;
    private int totalRecordsImpacted;
    private List<TableData> tableData;

    // Constructor
    public DashboardData(String lastSuccessfulRuntime, int totalTablesImpacted, int totalRecordsImpacted, List<TableData> tableData) {
        this.lastSuccessfulRuntime = lastSuccessfulRuntime;
        this.totalTablesImpacted = totalTablesImpacted;
        this.totalRecordsImpacted = totalRecordsImpacted;
        this.tableData = tableData;
    }

    // Getters and Setters
    public String getLastSuccessfulRuntime() {
        return lastSuccessfulRuntime;
    }

    public void setLastSuccessfulRuntime(String lastSuccessfulRuntime) {
        this.lastSuccessfulRuntime = lastSuccessfulRuntime;
    }

    public int getTotalTablesImpacted() {
        return totalTablesImpacted;
    }

    public void setTotalTablesImpacted(int totalTablesImpacted) {
        this.totalTablesImpacted = totalTablesImpacted;
    }

    public int getTotalRecordsImpacted() {
        return totalRecordsImpacted;
    }

    public void setTotalRecordsImpacted(int totalRecordsImpacted) {
        this.totalRecordsImpacted = totalRecordsImpacted;
    }

    public List<TableData> getTableData() {
        return tableData;
    }

    public void setTableData(List<TableData> tableData) {
        this.tableData = tableData;
    }
}
