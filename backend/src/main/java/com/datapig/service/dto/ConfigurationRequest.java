package com.datapig.service.dto; 

import java.util.List;

public class ConfigurationRequest {
    private String dataLakePath;
    private List<String> selectedTables;

    // Constructors, Getters, and Setters
    public ConfigurationRequest() {}

    public ConfigurationRequest(String dataLakePath, List<String> selectedTables) {
        this.dataLakePath = dataLakePath;
        this.selectedTables = selectedTables;
    }

    public String getDataLakePath() {
        return dataLakePath;
    }

    public void setDataLakePath(String dataLakePath) {
        this.dataLakePath = dataLakePath;
    }

    public List<String> getSelectedTables() {
        return selectedTables;
    }

    public void setSelectedTables(List<String> selectedTables) {
        this.selectedTables = selectedTables;
    }
}
