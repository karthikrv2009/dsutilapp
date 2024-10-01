package com.datapig.controller; 


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    @PostMapping("/setDataLakePath")
    public ResponseEntity<String> setDataLakePath(@RequestParam String dataLakePath) {
        // Log the received data lake path
        logger.info("Received Data Lake Path: {}", dataLakePath);

        // Validate and save the data lake path (dummy logic for now)
        if (dataLakePath == null || dataLakePath.isEmpty()) {
            return ResponseEntity.status(400).body("Invalid data lake path!");
        }
        return ResponseEntity.ok("Data lake path saved successfully!");
    }
}
