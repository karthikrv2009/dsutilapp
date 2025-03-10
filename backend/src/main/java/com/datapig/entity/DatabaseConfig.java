package com.datapig.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;

@Entity
@Table(name = "database_config")
public class DatabaseConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dbIdentifier", unique = true, nullable = false, length = 50)
    private String dbIdentifier;

    @Column(name = "url", nullable = false, length = 300)
    private String url;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 300)
    private String password;

    @Column(name = "driver_class_name", length = 300)
    private String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    @Column(name = "queue_name", length = 300)
    private String queueName;

    @Column(name = "queue_sas_token", length = 300)
    private String queueSasToken;

    @Column(name = "queue_endpoint", length = 300)
    private String queueEndpoint;

    @Column(name = "adls_storage_account_name", length = 300)
    private String adlsStorageAccountName;

    @Column(name = "adls_storage_account_endpoint", length = 300)
    private String adlsStorageAccountEndpoint;

    @Column(name = "adls_storage_account_sas_key", length = 300)
    private String adlsStorageAccountSasKey;

    @Column(name = "adls_container_name", length = 300)
    private String adlsContainerName;

    @Column(name = "adls_folder_name", length = 300)
    private String adlsFolderName;

    @Column(name = "adls_cdm_file_name", length = 300)
    private String adlsCdmFileName;

    @Column(name = "adls_cdm_file_path", length = 300)
    private String adlsCdmFilePath;

    @Column(name = "local_cdm_file_path", length = 300)
    private String localCdmFilePath;

    @Column(name = "max_threads")
    private int maxThreads;

    @Column(name = "enable_archive")
    private boolean enableArchive;

    @Column(name = "default_profile")
    @ColumnDefault("0")
    private boolean defaultProfile=false;

    @Column(name = "purge_enabled")
    @ColumnDefault("0")
    private boolean purgeEnabled=false;

    @Column(name = "purge_duration")
    @ColumnDefault("31622400000")
    private long purgeDuration = 31622400000L;

    @Column(name = "purge_unit_value")
    private long purgeUnitValue;

    @Column(name = "purge_unit")
    private String purgeUnit;

    public long getPurgeUnitValue() {
        return purgeUnitValue;
    }

    public void setPurgeUnitValue(long purgeUnitValue) {
        this.purgeUnitValue = purgeUnitValue;
    }

    public String getPurgeUnit() {
        return purgeUnit;
    }

    public void setPurgeUnit(String purgeUnit) {
        this.purgeUnit = purgeUnit;
    }

    public void setEnableArchive(boolean enableArchive) {
        this.enableArchive = enableArchive;
    }

    public long getPurgeDuration() {
        return purgeDuration;
    }

    public void setPurgeDuration(long purgeDuration) {
        this.purgeDuration = purgeDuration;
    }

    public boolean isPurgeEnabled() {
        return purgeEnabled;
    }

    public void setPurgeEnabled(boolean purgeEnabled) {
        this.purgeEnabled = purgeEnabled;
    }

    public boolean isEnableArchive() {
        return enableArchive;
    }

    public void setEnableArchive(Boolean enableArchive) {
        if (enableArchive == null) {
            // If the value is not set (null), assign default value of false (or 0)
            this.enableArchive = false;
        } else {
            this.enableArchive = enableArchive;
        }
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

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public boolean isDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(boolean defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    @Override
    public String toString() {
        return "DatabaseConfig [id=" + id + ", dbIdentifier=" + dbIdentifier + ", url=" + url + ", username=" + username
                + ", password=" + password + ", driverClassName=" + driverClassName + ", queueName=" + queueName
                + ", queueSasToken=" + queueSasToken + ", queueEndpoint=" + queueEndpoint + ", adlsStorageAccountName="
                + adlsStorageAccountName + ", adlsStorageAccountEndpoint=" + adlsStorageAccountEndpoint
                + ", adlsStorageAccountSasKey=" + adlsStorageAccountSasKey + ", adlsContainerName=" + adlsContainerName
                + ", adlsFolderName=" + adlsFolderName + ", adlsCdmFileName=" + adlsCdmFileName + ", adlsCdmFilePath="
                + adlsCdmFilePath + ", localCdmFilePath=" + localCdmFilePath + ", maxThreads=" + maxThreads
                + ", enableArchive=" + enableArchive + ", defaultProfile=" + defaultProfile + ", purgeEnabled="
                + purgeEnabled + ", purgeDuration=" + purgeDuration + ", getClass()=" + getClass()
                + ", getPurgeDuration()=" + getPurgeDuration() + ", isPurgeEnabled()=" + isPurgeEnabled()
                + ", isEnableArchive()=" + isEnableArchive() + ", getQueueName()=" + getQueueName()
                + ", getQueueSasToken()=" + getQueueSasToken() + ", getQueueEndpoint()=" + getQueueEndpoint()
                + ", getAdlsStorageAccountName()=" + getAdlsStorageAccountName() + ", getAdlsStorageAccountEndpoint()="
                + getAdlsStorageAccountEndpoint() + ", hashCode()=" + hashCode() + ", getAdlsStorageAccountSasKey()="
                + getAdlsStorageAccountSasKey() + ", getAdlsContainerName()=" + getAdlsContainerName()
                + ", getAdlsFolderName()=" + getAdlsFolderName() + ", getAdlsCdmFileName()=" + getAdlsCdmFileName()
                + ", getAdlsCdmFilePath()=" + getAdlsCdmFilePath() + ", getLocalCdmFilePath()=" + getLocalCdmFilePath()
                + ", getId()=" + getId() + ", getDbIdentifier()=" + getDbIdentifier() + ", getUrl()=" + getUrl()
                + ", getUsername()=" + getUsername() + ", getPassword()=" + getPassword() + ", getDriverClassName()="
                + getDriverClassName() + ", getMaxThreads()=" + getMaxThreads() + ", isDefaultProfile()="
                + isDefaultProfile() + ", toString()=" + super.toString() + "]";
    }

}
