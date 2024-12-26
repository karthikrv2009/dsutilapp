package com.datapig.component;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

@Component
public class DataSourceConfig {
    public DataSource createDataSource(String dbIdentifier) {
        // Load the properties for the specific database identifier.
        Properties dbProperties = loadDatabaseProperties(dbIdentifier);

        // Create and configure a HikariDataSource instance.
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dbProperties.getProperty("url"));
        dataSource.setUsername(dbProperties.getProperty("username"));
        dataSource.setPassword(dbProperties.getProperty("password"));
        dataSource.setDriverClassName(dbProperties.getProperty("driverClassName"));

        return dataSource;
    }
}
