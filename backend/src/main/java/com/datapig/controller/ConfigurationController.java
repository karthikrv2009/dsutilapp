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

@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    @Autowired
    private ConfigurationRepository configurationRepository;


   @PostMapping("/saveConfiguration")
    public ResponseEntity< Map<String, String>> saveConfiguration(@RequestBody ConfigurationRequest configRequest) {
        String dataLakePath = configRequest.getDataLakePath();
        String selectedTables = String.join(",",configRequest.getSelectedTables());
        Map<String, String> response = new HashMap<>();
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
}
