package com.datapig.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.datapig.service.LicenseKeyService;
import com.datapig.entity.LicenseKey;
import com.datapig.entity.EnvironmentConfig;
import com.datapig.service.EnvironmentConfigService;

import java.util.List;

@RestController
@RequestMapping("/api/license")
public class LicenseKeyController {

    @Autowired
    private LicenseKeyService licenseKeyService;

    @Autowired
    private EnvironmentConfigService environmentConfigService;

    // GET method for /api/license
    @GetMapping
    public ResponseEntity<List<LicenseKey>> getAllLicenseKeys() {
        List<LicenseKey> licenseKeys = licenseKeyService.getAllLicenseKeys();
        return ResponseEntity.ok(licenseKeys);
    }

    // POST method for /api/license
    @PostMapping
    public ResponseEntity<LicenseKey> createLicenseKey(@RequestBody LicenseKey licenseKey) {
        LicenseKey savedLicenseKey = licenseKeyService.saveLicenseKey(licenseKey);

        return ResponseEntity.ok(savedLicenseKey);
    }

    // GET method for /api/license/environment
    @GetMapping("/environment")
    public ResponseEntity<List<EnvironmentConfig>> getAllEnvironmentConfigs() {
        List<EnvironmentConfig> environmentConfigs = environmentConfigService.getAllEnvironmentConfigs();
        return ResponseEntity.ok(environmentConfigs);
    }

    // POST method for /api/license/environment
    @PostMapping("/environment")
    public ResponseEntity<EnvironmentConfig> createEnvironmentConfig(@RequestBody EnvironmentConfig environmentConfig) {
        EnvironmentConfig savedEnvironmentConfig = environmentConfigService.saveEnvironmentConfig(environmentConfig);
        return ResponseEntity.ok(savedEnvironmentConfig);
    }
}