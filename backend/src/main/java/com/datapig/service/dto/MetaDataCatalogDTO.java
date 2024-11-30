package com.datapig.service.dto;

public class MetaDataCatalogDTO {
    private String tableName;
    private String lastUpdatedFolder;
    private short lastCopyStatus;
    private int quarintine;
    private int rowCount;
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public String getLastUpdatedFolder() {
        return lastUpdatedFolder;
    }
    public void setLastUpdatedFolder(String lastUpdatedFolder) {
        this.lastUpdatedFolder = lastUpdatedFolder;
    }
    public short getLastCopyStatus() {
        return lastCopyStatus;
    }
    public void setLastCopyStatus(short lastCopyStatus) {
        this.lastCopyStatus = lastCopyStatus;
    }
    public int getQuarintine() {
        return quarintine;
    }
    public void setQuarintine(int quarintine) {
        this.quarintine = quarintine;
    }
    public int getRowCount() {
        return rowCount;
    }
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

}
