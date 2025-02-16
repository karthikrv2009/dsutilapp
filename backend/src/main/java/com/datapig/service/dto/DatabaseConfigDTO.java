package com.datapig.service.dto;

import com.datapig.entity.DatabaseConfig;

public class DatabaseConfigDTO {

    private Long id;

    private String dbIdentifier;

    private String url;

    private String username;

    private String password;

    private String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private String queueName;

    private String queueSasToken;

    private String queueEndpoint;

    private String adlsStorageAccountName;

    private String adlsStorageAccountEndpoint;

    private String adlsStorageAccountSasKey;

    private String adlsContainerName;

    private String adlsFolderName;

    private String adlsCdmFileName;

    private String adlsCdmFilePath;

    private String localCdmFilePath;

    private int maxThreads;

    private boolean enableArchive;

    private int initialLoadStatus;

    private int queueListenerStatus;

    private boolean defaultProfile;

    private boolean purgeEnabled;

    private long purgeDuration;

    private String purgeUnit = "days"; // Initialize with a default value

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDbIdentifier() {
        return dbIdentifier;
    }

    public void setDbIdentifier(String dbIdentifier) {
        this.dbIdentifier = dbIdentifier;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueSasToken() {
        return queueSasToken;
    }

    public void setQueueSasToken(String queueSasToken) {
        this.queueSasToken = queueSasToken;
    }

    public String getQueueEndpoint() {
        return queueEndpoint;
    }

    public void setQueueEndpoint(String queueEndpoint) {
        this.queueEndpoint = queueEndpoint;
    }

    public String getAdlsStorageAccountName() {
        return adlsStorageAccountName;
    }

    public void setAdlsStorageAccountName(String adlsStorageAccountName) {
        this.adlsStorageAccountName = adlsStorageAccountName;
    }

    public String getAdlsStorageAccountEndpoint() {
        return adlsStorageAccountEndpoint;
    }

    public void setAdlsStorageAccountEndpoint(String adlsStorageAccountEndpoint) {
        this.adlsStorageAccountEndpoint = adlsStorageAccountEndpoint;
    }

    public String getAdlsStorageAccountSasKey() {
        return adlsStorageAccountSasKey;
    }

    public void setAdlsStorageAccountSasKey(String adlsStorageAccountSasKey) {
        this.adlsStorageAccountSasKey = adlsStorageAccountSasKey;
    }

    public String getAdlsContainerName() {
        return adlsContainerName;
    }

    public void setAdlsContainerName(String adlsContainerName) {
        this.adlsContainerName = adlsContainerName;
    }

    public String getAdlsFolderName() {
        return adlsFolderName;
    }

    public void setAdlsFolderName(String adlsFolderName) {
        this.adlsFolderName = adlsFolderName;
    }

    public String getAdlsCdmFileName() {
        return adlsCdmFileName;
    }

    public void setAdlsCdmFileName(String adlsCdmFileName) {
        this.adlsCdmFileName = adlsCdmFileName;
    }

    public String getAdlsCdmFilePath() {
        return adlsCdmFilePath;
    }

    public void setAdlsCdmFilePath(String adlsCdmFilePath) {
        this.adlsCdmFilePath = adlsCdmFilePath;
    }

    public String getLocalCdmFilePath() {
        return localCdmFilePath;
    }

    public void setLocalCdmFilePath(String localCdmFilePath) {
        this.localCdmFilePath = localCdmFilePath;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getInitialLoadStatus() {
        return initialLoadStatus;
    }

    public void setInitialLoadStatus(int initialLoadStatus) {
        this.initialLoadStatus = initialLoadStatus;
    }

    public int getQueueListenerStatus() {
        return queueListenerStatus;
    }

    public void setQueueListenerStatus(int queueListenerStatus) {
        this.queueListenerStatus = queueListenerStatus;
    }

    // Method to convert purgeDuration and purgeUnit to milliseconds
    public long getPurgeDurationInMilliseconds() {
        long durationInMilliseconds = 0;
        switch (purgeUnit.toLowerCase()) {
            case "weeks":
                durationInMilliseconds = purgeDuration * 7L * 24L * 60L * 60L * 1000L;
                break;
            case "days":
                durationInMilliseconds = purgeDuration * 24L * 60L * 60L * 1000L;
                break;
            case "months":
                durationInMilliseconds = purgeDuration * 30L * 24L * 60L * 60L * 1000L;
                break;
            case "years":
                durationInMilliseconds = purgeDuration * 365L * 24L * 60L * 60L * 1000L;
                break;
            default:
                throw new IllegalArgumentException("Invalid purge unit: " + purgeUnit);
        }
        return durationInMilliseconds;
    }

    // Method to convert milliseconds to purgeDuration and purgeUnit
    public void setPurgeDurationFromMilliseconds(long milliseconds) {
        if (milliseconds % (365L * 24L * 60L * 60L * 1000L) == 0) {
            this.purgeUnit = "years";
            this.purgeDuration = milliseconds / (365L * 24L * 60L * 60L * 1000L);
        } else if (milliseconds % (30L * 24L * 60L * 60L * 1000L) == 0) {
            this.purgeUnit = "months";
            this.purgeDuration = milliseconds / (30L * 24L * 60L * 60L * 1000L);
        } else if (milliseconds % (7L * 24L * 60L * 60L * 1000L) == 0) {
            this.purgeUnit = "weeks";
            this.purgeDuration = milliseconds / (7L * 24L * 60L * 60L * 1000L);
        } else if (milliseconds % (24L * 60L * 60L * 1000L) == 0) {
            this.purgeUnit = "days";
            this.purgeDuration = milliseconds / (24L * 60L * 60L * 1000L);
        } else {
            throw new IllegalArgumentException("Invalid duration in milliseconds: " + milliseconds);
        }
    }

    // Method to convert DatabaseConfig to DatabaseConfigDTO
    public DatabaseConfigDTO fromEntity(DatabaseConfig databaseConfig, int initialLoadStatus,
            int queueListenerStatus) {
        DatabaseConfigDTO dto = new DatabaseConfigDTO();
        dto.setId(databaseConfig.getId());
        dto.setDbIdentifier(databaseConfig.getDbIdentifier());
        dto.setUrl(databaseConfig.getUrl());
        dto.setUsername(databaseConfig.getUsername());
        dto.setPassword(databaseConfig.getPassword());
        dto.setDriverClassName(databaseConfig.getDriverClassName());
        dto.setQueueName(databaseConfig.getQueueName());
        dto.setQueueSasToken(databaseConfig.getQueueSasToken());
        dto.setQueueEndpoint(databaseConfig.getQueueEndpoint());
        dto.setAdlsStorageAccountName(databaseConfig.getAdlsStorageAccountName());
        dto.setAdlsStorageAccountEndpoint(databaseConfig.getAdlsStorageAccountEndpoint());
        dto.setAdlsStorageAccountSasKey(databaseConfig.getAdlsStorageAccountSasKey());
        dto.setAdlsContainerName(databaseConfig.getAdlsContainerName());
        dto.setAdlsFolderName(databaseConfig.getAdlsFolderName());
        dto.setAdlsCdmFileName(databaseConfig.getAdlsCdmFileName());
        dto.setAdlsCdmFilePath(databaseConfig.getAdlsCdmFilePath());
        dto.setLocalCdmFilePath(databaseConfig.getLocalCdmFilePath());
        dto.setMaxThreads(databaseConfig.getMaxThreads());
        dto.setInitialLoadStatus(initialLoadStatus);
        dto.setQueueListenerStatus(queueListenerStatus);
        dto.setEnableArchive(databaseConfig.isEnableArchive());
        dto.setPurgeEnabled(databaseConfig.isPurgeEnabled());
        dto.setPurgeDurationFromMilliseconds(databaseConfig.getPurgeDuration());
        // dto.setPurgeDuration(databaseConfig.getPurgeDuration());
        dto.setDefaultProfile(databaseConfig.isDefaultProfile());
        return dto;
    }

    public DatabaseConfig toEntity() {
        DatabaseConfig entity = new DatabaseConfig();
        entity.setId(this.getId());
        entity.setDbIdentifier(this.getDbIdentifier());
        entity.setUrl(this.getUrl());
        entity.setUsername(this.getUsername());
        entity.setPassword(this.getPassword());
        entity.setDriverClassName(this.getDriverClassName());
        entity.setQueueName(this.getQueueName());
        entity.setQueueSasToken(this.getQueueSasToken());
        entity.setQueueEndpoint(this.getQueueEndpoint());
        entity.setAdlsStorageAccountName(this.getAdlsStorageAccountName());
        entity.setAdlsStorageAccountEndpoint(this.getAdlsStorageAccountEndpoint());
        entity.setAdlsStorageAccountSasKey(this.getAdlsStorageAccountSasKey());
        entity.setAdlsContainerName(this.getAdlsContainerName());
        entity.setAdlsFolderName(this.getAdlsFolderName());
        entity.setAdlsCdmFileName(this.getAdlsCdmFileName());
        entity.setAdlsCdmFilePath(this.getAdlsCdmFilePath());
        entity.setLocalCdmFilePath(this.getLocalCdmFilePath());
        entity.setMaxThreads(this.getMaxThreads());
        entity.setEnableArchive(this.isEnableArchive());
        entity.setDefaultProfile(this.isDefaultProfile());
        entity.setPurgeEnabled(this.isPurgeEnabled());
        entity.setPurgeDuration(this.getPurgeDurationInMilliseconds());
        return entity;
    }

    public boolean isEnableArchive() {
        return enableArchive;
    }

    public void setEnableArchive(boolean enableArchive) {
        this.enableArchive = enableArchive;
    }

    public boolean isDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(boolean defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    public boolean isPurgeEnabled() {
        return purgeEnabled;
    }

    public void setPurgeEnabled(boolean purgeEnabled) {
        this.purgeEnabled = purgeEnabled;
    }

    public long getPurgeDuration() {
        return purgeDuration;
    }

    public void setPurgeDuration(long purgeDuration) {
        this.purgeDuration = purgeDuration;
    }

    public String getPurgeUnit() {
        return purgeUnit;
    }

    public void setPurgeUnit(String purgeUnit) {
        this.purgeUnit = purgeUnit;
    }

}
