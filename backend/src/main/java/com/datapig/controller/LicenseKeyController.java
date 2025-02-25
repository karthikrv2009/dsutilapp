package com.datapig.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.datapig.service.LicenseKeyService;
import com.datapig.entity.LicenseKey;
import com.datapig.entity.EnvironmentConfig;
import com.datapig.service.EnvironmentConfigService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/license")
public class LicenseKeyController {

    @Autowired
    private LicenseKeyService licenseKeyService;

    @Autowired
    private EnvironmentConfigService environmentConfigService;

    // GET method for /api/license
    @GetMapping
    public ResponseEntity<LicenseKey> getAllLicenseKeys() {
        List<LicenseKey> licenseKeys = licenseKeyService.getAllLicenseKeys();
        if (licenseKeys.size() == 0) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(licenseKeys.get(0));
    }

    // POST method for /api/license
    @PostMapping
    public ResponseEntity<LicenseKey> createLicenseKey(@RequestBody LicenseKey licenseKey) {

        licenseKeyService.deleteLicense();

        LicenseKey savedLicenseKey = licenseKeyService.saveLicenseKey(licenseKey);

        return ResponseEntity.ok(savedLicenseKey);
    }

    // GET method for /api/license/environment
    @GetMapping("/environment")
    public ResponseEntity<EnvironmentConfig> getAllEnvironmentConfigs() {
        List<EnvironmentConfig> environmentConfigs = environmentConfigService.getAllEnvironmentConfigs();
        if (environmentConfigs.size() == 0) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(environmentConfigs.get(0));
    }

    @PostMapping("/environment/save")
    public ResponseEntity<EnvironmentConfig> createEnvironmentConfig(@RequestBody EnvironmentConfig environmentConfig) {
        EnvironmentConfig savedEnvironmentConfig = environmentConfigService.saveEnvironmentConfig(environmentConfig);
        return ResponseEntity.ok(savedEnvironmentConfig);
    }

    @PutMapping("/environment/{d365Environment}")
    public ResponseEntity<String> updateEnvironmentConfigs(@PathVariable String d365Environment,
            @RequestBody EnvironmentConfig environmentConfig) {
        try {
            environmentConfigService.updateEnvironmentConfigs(d365Environment, environmentConfig);
            return ResponseEntity.ok("Database config updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating database config");
        }
    }

    @PostMapping("/environment/validate")
    public ResponseEntity<Map<String, Boolean>> validateEnvironmentConfig(
            @RequestBody EnvironmentConfig environmentConfig) {
        Map<String, Boolean> validationResults = new HashMap<>();

        // Perform validation checks
        validationResults.put("d365Environment",
                environmentConfig.getD365Environment() != null && !environmentConfig.getD365Environment().isEmpty());
        validationResults.put("d365EnvironmentUrl", environmentConfig.getD365EnvironmentUrl() != null
                && !environmentConfig.getD365EnvironmentUrl().isEmpty());
        validationResults.put("stringOffSet",
                environmentConfig.getStringOffSet() != null && !environmentConfig.getStringOffSet().isEmpty());
        validationResults.put("maxLength", environmentConfig.getMaxLength() > 0);
        validationResults.put("stringOutlierPath", environmentConfig.getStringOutlierPath() != null
                && !environmentConfig.getStringOutlierPath().isEmpty());

        return ResponseEntity.ok(validationResults);
    }
}