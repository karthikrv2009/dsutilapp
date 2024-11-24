package com.datapig.service.dto;

import java.util.List;
import java.util.Set;

import com.datapig.entity.FolderSyncStatus;

public class FolderSyncStatusDTO {
    private String currentPackageName;
    private int pendingPackages;
    private int inProgressTables;
    private List<FolderSyncStatus> currentProcessingTables;

    private int pendingTables;
    private int completedTables;
    private int completedPackages;

    private int errorTablesCount;
    private Set<String> errorTablesName;

    public int getErrorTablesCount() {
        return errorTablesCount;
    }
    public void setErrorTablesCount(int errorTablesCount) {
        this.errorTablesCount = errorTablesCount;
    }
    public Set<String> getErrorTablesName() {
        return errorTablesName;
    }
    public void setErrorTablesName(Set<String> errorTablesName) {
        this.errorTablesName = errorTablesName;
    }
    public int getCompletedPackages() {
        return completedPackages;
    }
    public void setCompletedPackages(int completedPackages) {
        this.completedPackages = completedPackages;
    }
    public int getPendingPackages() {
        return pendingPackages;
    }
    public void setPendingPackages(int pendingPackages) {
        this.pendingPackages = pendingPackages;
    }

    public String getCurrentPackageName() {
        return currentPackageName;
    }
    public void setCurrentPackageName(String currentPackageName) {
        this.currentPackageName = currentPackageName;
    }
    public int getCompletedTables() {
        return completedTables;
    }
    public void setCompletedTables(int completedTables) {
        this.completedTables = completedTables;
    }
    public int getPendingTables() {
        return pendingTables;
    }
    public void setPendingTables(int pendingTables) {
        this.pendingTables = pendingTables;
    }
    public int getInProgressTables() {
        return inProgressTables;
    }
    public void setInProgressTables(int inProgressTables) {
        this.inProgressTables = inProgressTables;
    }
    public List<FolderSyncStatus> getCurrentProcessingTables() {
        return currentProcessingTables;
    }
    public void setCurrentProcessingTables(List<FolderSyncStatus> currentProcessingTables) {
        this.currentProcessingTables = currentProcessingTables;
    }
    
    
    
    }
