package com.datapig.controller;

import com.datapig.service.dto.LicenseKeyDTO;
import com.datapig.service.LicenseKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/license")
public class LicenseKeyController {

    private static final Logger logger = LoggerFactory.getLogger(LicenseKeyController.class);
    private final LicenseKeyService licenseKeyService;

    public LicenseKeyController(LicenseKeyService licenseKeyService) {
        this.licenseKeyService = licenseKeyService;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateLicenseKey(@RequestParam String licenseKey) {
        // Log the received license key
        logger.info("Received license key: {}", licenseKey);

        // Validate license key
        LicenseKeyDTO licenseKeyDTO = licenseKeyService.validateLicenseKey(licenseKey);

        if (licenseKeyDTO != null) {
            return ResponseEntity.ok(licenseKeyDTO);
        } else {
            return ResponseEntity.status(400).body("Invalid license key!");
        }
    }
}