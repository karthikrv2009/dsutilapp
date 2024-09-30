package com.datapig.controller; 

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    @PostMapping("/setDataLakePath")
    public ResponseEntity<String> setDataLakePath(@RequestParam String dataLakePath) {
        // Assume the data lake path is always valid for now
        if (dataLakePath == null || dataLakePath.isEmpty()) {
            return ResponseEntity.status(400).body("Invalid data lake path!");
        }
        return ResponseEntity.ok("Data lake path saved successfully!");
    }
}
