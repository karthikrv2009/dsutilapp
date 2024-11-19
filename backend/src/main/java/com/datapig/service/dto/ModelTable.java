package com.datapig.service.dto; 

import java.util.List;

public class ModelTable {
   
    private String tableName;
    private List<ModelTableAttributes> attributes;

    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public List<ModelTableAttributes> getAttributes() {
        return attributes;
    }
    public void setAttributes(List<ModelTableAttributes> attributes) {
        this.attributes = attributes;
    }

    
}
