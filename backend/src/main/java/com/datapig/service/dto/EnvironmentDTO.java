package com.datapig.service.dto;

public class EnvironmentDTO {

    private String d365Environment;
    private String d365EnvironmentURL;
    private String adlsStorageAccount;
    private String containerName;
    private int max_thread_count;
    
    public String getD365Environment() {
        return d365Environment;
    }
    public void setD365Environment(String d365Environment) {
        this.d365Environment = d365Environment;
    }
    public String getD365EnvironmentURL() {
        return d365EnvironmentURL;
    }
    public void setD365EnvironmentURL(String d365EnvironmentURL) {
        this.d365EnvironmentURL = d365EnvironmentURL;
    }
    public String getAdlsStorageAccount() {
        return adlsStorageAccount;
    }
    public void setAdlsStorageAccount(String adlsStorageAccount) {
        this.adlsStorageAccount = adlsStorageAccount;
    }
    public String getContainerName() {
        return containerName;
    }
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    public int getMax_thread_count() {
        return max_thread_count;
    }
    public void setMax_thread_count(int max_thread_count) {
        this.max_thread_count = max_thread_count;
    }

}
