package com.datapig.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

@Component
public class EncryptedPropertyReader {

    private final Properties properties = new Properties();

    public EncryptedPropertyReader(@Value("${config.filepath}")
        String filePath) {
        
        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            
            // Read the base64-encoded content
            byte[] base64EncodedBytes = inputStream.readAllBytes();
            /* 
            // Decode the content
            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedBytes);
            // Convert decoded bytes to string and load into properties
            */
            String decodedContent = new String(base64EncodedBytes, StandardCharsets.UTF_8);
            properties.load(new java.io.StringReader(decodedContent));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load and decode the properties file.");
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
