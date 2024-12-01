package com.datapig.service.dto;

public class DBSnapshotWidget {

    private String lastProcessedfolder;
    private String latestADLSFolderAvailable;
    private long completedPackages;
    private long pendingNumberPackages;
    private long pendingTablesInAllPackages;

    

    public String getLastProcessedfolder() {
        return lastProcessedfolder;
    }

    public void setLastProcessedfolder(String lastProcessedfolder) {
        this.lastProcessedfolder = lastProcessedfolder;
    }

    public String getLatestADLSFolderAvailable() {
        return latestADLSFolderAvailable;
    }

    public void setLatestADLSFolderAvailable(String latestADLSFolderAvailable) {
        this.latestADLSFolderAvailable = latestADLSFolderAvailable;
    }

    public long getPendingNumberPackages() {
        return pendingNumberPackages;
    }

    public void setPendingNumberPackages(long pendingNumberPackages) {
        this.pendingNumberPackages = pendingNumberPackages;
    }

    public long getPendingTablesInAllPackages() {
        return pendingTablesInAllPackages;
    }

    public void setPendingTablesInAllPackages(long pendingTablesInAllPackages) {
        this.pendingTablesInAllPackages = pendingTablesInAllPackages;
    }

    public long getCompletedPackages() {
        return completedPackages;
    }

    public void setCompletedPackages(long completedPackages) {
        this.completedPackages = completedPackages;
    }
    
}
