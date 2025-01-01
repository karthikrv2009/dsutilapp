package com.datapig.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "environment_config")
public class EnvironmentConfig {

    @Id
    @Column(name = "d365_environment", length = 200)
    private String d365Environment;

    @Column(name = "d365_environment_url", length = 300)
    private String d365EnvironmentUrl;

    @Column(name = "string_offset", length = 100)
    private String stringOffSet;

    @Column(name = "max_length")
    private int maxLength;

    @Column(name = "string_outlier_path", length = 100)
    private String stringOutlierPath;

    public String getD365Environment() {
        return d365Environment;
    }

    public void setD365Environment(String d365Environment) {
        this.d365Environment = d365Environment;
    }

    public String getD365EnvironmentUrl() {
        return d365EnvironmentUrl;
    }

    public void setD365EnvironmentUrl(String d365EnvironmentUrl) {
        this.d365EnvironmentUrl = d365EnvironmentUrl;
    }

    public String getStringOffSet() {
        return stringOffSet;
    }

    public void setStringOffSet(String stringOffSet) {
        this.stringOffSet = stringOffSet;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getStringOutlierPath() {
        return stringOutlierPath;
    }

    public void setStringOutlierPath(String stringOutlierPath) {
        this.stringOutlierPath = stringOutlierPath;
    }

}
