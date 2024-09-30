package com.datapig.controller; 

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/license")
public class LicenseKeyController {

    @PostMapping("/validate")
    public ResponseEntity<String> validateLicenseKey(@RequestParam String licenseKey) {
        // For now, assume validation always succeeds
        boolean isValid = licenseKey != null && !licenseKey.isEmpty();
        
        if (isValid) {
            return ResponseEntity.ok("License key validated successfully!");
        } else {
            return ResponseEntity.status(400).body("Invalid license key!");
        }
    }
}
