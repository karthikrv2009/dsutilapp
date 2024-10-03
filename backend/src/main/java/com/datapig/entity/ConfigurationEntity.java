package com.datapig.entity; 
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.util.List;

@Entity
public class ConfigurationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String licenseKey;
    private String dataLakePath;
    
    // Store selected tables as comma-separated values
    private String selectedTables;

    // Constructors, getters, and setters

    public ConfigurationEntity() {}

    public ConfigurationEntity(String licenseKey, String dataLakePath, String selectedTables) {
        this.licenseKey = licenseKey;
        this.dataLakePath = dataLakePath;
        this.selectedTables = selectedTables;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getDataLakePath() {
        return dataLakePath;
    }

    public void setDataLakePath(String dataLakePath) {
        this.dataLakePath = dataLakePath;
    }

    public String getSelectedTables() {
        return selectedTables;
    }

    public void setSelectedTables(String selectedTables) {
        this.selectedTables = selectedTables;
    }
}
