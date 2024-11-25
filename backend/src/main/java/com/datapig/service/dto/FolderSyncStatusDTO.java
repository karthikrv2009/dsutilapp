package com.datapig.service.dto;

public class FolderSyncStatusDTO {
    private String currentPackageName;
    private int inProgressTables;
    private int pendingTables;
    private int completedTables;
    private int errorTablesCount;

    public int getErrorTablesCount() {
        return errorTablesCount;
    }
    public void setErrorTablesCount(int errorTablesCount) {
        this.errorTablesCount = errorTablesCount;
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
   }
