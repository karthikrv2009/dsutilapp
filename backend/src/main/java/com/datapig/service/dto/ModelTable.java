package com.datapig.service.dto; 

public class ModelTable {
   
    private String tableName;
    private String attributeName;
    private String dataType;
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public String getAttributeName() {
        return attributeName;
    }
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
