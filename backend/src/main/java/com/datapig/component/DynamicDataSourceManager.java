package com.datapig.component;

import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

@Component
public class DynamicDataSourceManager {

    private final ConcurrentHashMap<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    public DataSource getDataSource(String dbIdentifier) {
        return dataSourceMap.get(dbIdentifier);
    }

    public void addDataSource(String dbIdentifier, String url, String username, String password) {
        if (dataSourceMap.containsKey(dbIdentifier)) {
           // throw new IllegalArgumentException("DataSource with identifier " + dbIdentifier + " already exists.");
            return;
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        // dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        dataSourceMap.put(dbIdentifier, dataSource);
    }

    public void removeDataSource(String dbIdentifier) {
        dataSourceMap.remove(dbIdentifier);
    }

}
