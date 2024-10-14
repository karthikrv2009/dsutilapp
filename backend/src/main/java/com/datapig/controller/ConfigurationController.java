package com.datapig.controller; 


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.datapig.service.dto.ConfigurationRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.datapig.entity.ConfigurationEntity;
import com.datapig.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import com.datapig.service.PropertiesService;

@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private PropertiesService propertiesService;


   @PostMapping("/saveConfiguration")
    public ResponseEntity< Map<String, String>> saveConfiguration(@RequestBody ConfigurationRequest configRequest) {
        String dataLakePath = configRequest.getDataLakePath();
        String selectedTables = String.join(",",configRequest.getSelectedTables());
        Map<String, String> response = new HashMap<>();
        String licenseKey="12345";
        response.put("message", "Configuration saved successfully!");

        // Here you would handle saving the configuration (e.g., saving to a database)
        // Example:
        // configurationService.saveDataLakePath(dataLakePath);
        // configurationService.saveSelectedTables(selectedTables);

        // Save configuration to the database
        ConfigurationEntity configurationEntity = new ConfigurationEntity(licenseKey, dataLakePath, selectedTables);
        configurationRepository.save(configurationEntity);
        
        System.out.println("Data Lake Path: " + dataLakePath);
        System.out.println("Selected Tables: " + selectedTables);
        System.out.println("Configuration saved successfully!");

        return ResponseEntity.ok(response);
    }


    @PostMapping("/saveProperties")
    public ResponseEntity<String> saveProperties(@RequestBody Map<String, String> properties) {
        propertiesService.saveProperties(properties);
        return ResponseEntity.ok("Properties saved successfully");
    }

     @GetMapping("/getProperties")
    public ResponseEntity<Map<String, String>> getProperties() {
        // Logic to fetch properties from DB or config
       Map<String, String> properties = propertiesService.getAllProperties();
       Map<String, String> defaultProperties = new HashMap<>();
       if (properties.isEmpty()) {
        // If no properties are found, return the default properties
        defaultProperties.put("QUEUE_NAME", " ");
        defaultProperties.put("Queue_SAS_TOKEN", " ");
        defaultProperties.put("SAS_QUEUE_URL", " ");
        defaultProperties.put("DATA_SOURCE", " ");
        defaultProperties.put("STRORAGE_ACCOUNT_URL", " ");
        defaultProperties.put("Storage_SAS_TOKEN", " ");
        defaultProperties.put("BLOB_NAME", " ");
        defaultProperties.put("LOCAL_CHANGE_LOG", " ");
        defaultProperties.put("LOCAL_MOLDEL_JSON", " ");
        defaultProperties.put("ENVIRONMENT", " ");
        defaultProperties.put("STRING_OFFSET", " ");
        defaultProperties.put("STRING_MAXLENGTH", " ");
        defaultProperties.put("STRING_OUTLIER_PATH", " ");
        defaultProperties.put("STORAGE_ACCOUNT", " ");
        return ResponseEntity.ok(defaultProperties);
    }else {
        return ResponseEntity.ok(properties);
    }   
    }

}
