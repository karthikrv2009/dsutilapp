package com.datapig.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class PropertiesFileReader {

    private Properties properties;

    public PropertiesFileReader(String filePath) {
        properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static HashMap<String,String> getConfigs() {
        HashMap<String,String> configs=new HashMap<String,String>();
    	
    	PropertiesFileReader reader = new PropertiesFileReader("C:\\DataSync\\security\\config.properties");

        // Example of retrieving properties
        String QUEUE_NAME = reader.getProperty("QUEUE_NAME");
        String Queue_SAS_TOKEN = reader.getProperty("Queue_SAS_TOKEN");
        String SAS_QUEUE_URL = reader.getProperty("SAS_QUEUE_URL");
        String DATA_SOURCE = reader.getProperty("DATA_SOURCE");
        String STRORAGE_ACCOUNT_URL = reader.getProperty("STRORAGE_ACCOUNT_URL");
        String Storage_SAS_TOKEN = reader.getProperty("Queue_SAS_TOKEN");
        String BLOB_NAME = reader.getProperty("BLOB_NAME");
        String LOCAL_CHANGE_LOG = reader.getProperty("LOCAL_CHANGE_LOG");
        String LOCAL_MOLDEL_JSON = reader.getProperty("LOCAL_MOLDEL_JSON");
        String ENVIRONMENT = reader.getProperty("ENVIRONMENT");
        String DB_URL = reader.getProperty("DB_URL");
        String USERNAME = reader.getProperty("USERNAME");
        String PASSWORD = reader.getProperty("PASSWORD");
        String STRING_OFFSET=reader.getProperty("STRING_OFFSET");
        String STRING_MAXLENGTH=reader.getProperty("STRING_MAXLENGTH");
        String STRING_OUTLIER_PATH=reader.getProperty("STRING_OUTLIER_PATH");
        String STORAGE_ACCOUNT=reader.getProperty("STORAGE_ACCOUNT");
        
        configs.put("QUEUE_NAME", QUEUE_NAME);
        configs.put("Queue_SAS_TOKEN", Queue_SAS_TOKEN);
        configs.put("SAS_QUEUE_URL", SAS_QUEUE_URL);
        configs.put("DATA_SOURCE", DATA_SOURCE);
        configs.put("STRORAGE_ACCOUNT_URL", STRORAGE_ACCOUNT_URL);
        configs.put("Storage_SAS_TOKEN", Storage_SAS_TOKEN);
        configs.put("BLOB_NAME", BLOB_NAME);
        configs.put("LOCAL_CHANGE_LOG", LOCAL_CHANGE_LOG);
        configs.put("LOCAL_MOLDEL_JSON", LOCAL_MOLDEL_JSON);
        configs.put("ENVIRONMENT", ENVIRONMENT);
        configs.put("DB_URL", DB_URL);
        configs.put("USERNAME", USERNAME);
        configs.put("PASSWORD", PASSWORD);
        configs.put("STRING_OFFSET", STRING_OFFSET);
        configs.put("STRING_MAXLENGTH", STRING_MAXLENGTH);
        configs.put("STRING_OUTLIER_PATH", STRING_OUTLIER_PATH);
        configs.put("STORAGE_ACCOUNT", STORAGE_ACCOUNT);
        
        return configs;
    }
}

