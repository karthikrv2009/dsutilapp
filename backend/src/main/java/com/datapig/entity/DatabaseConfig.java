package com.datapig.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Table;
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

<<<<<<< HEAD
    @Column(name = "archive_enabled")
    private boolean archiveEnabled;

    @Column(name = "archive_days")
    private int archiveDays;

    public boolean isArchiveEnabled() {
        return archiveEnabled;
    }

    public void setArchiveEnabled(boolean archiveEnabled) {
        this.archiveEnabled = archiveEnabled;
    }

    public int getArchiveDays() {
        return archiveDays;
    }

    public void setArchiveDays(int archiveDays) {
        this.archiveDays = archiveDays;
=======
    @Column(name = "enable_archive")
    private boolean enableArchive;

    public boolean isEnableArchive() {
        return enableArchive;
    }

    public void setEnableArchive(boolean enableArchive) {
        this.enableArchive = enableArchive;
>>>>>>> 6fbc2f9d58ec0e81abcd01880ec4f27ed811b4f9
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

}
