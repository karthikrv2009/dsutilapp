package com.datapig.controller; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/license")
public class LicenseKeyController {

    private static final Logger logger = LoggerFactory.getLogger(LicenseKeyController.class);

    @PostMapping("/validate")
    public ResponseEntity<String> validateLicenseKey(@RequestParam String licenseKey) {
        // Log the received license key
        logger.info("Received license key: {}", licenseKey);

        // Validate license key
        boolean isValid = licenseKey != null && !licenseKey.isEmpty();
        
        if (isValid) {
            return ResponseEntity.ok("License key validated successfully!");
        } else {
            return ResponseEntity.status(400).body("Invalid license key!");
        }
    }
}
