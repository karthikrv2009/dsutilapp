package com.datapig.service;

import com.datapig.service.dto.LicenseKeyDTO;
import com.datapig.entity.LicenseKey;
import com.datapig.repository.LicenseKeyRepository;
import org.springframework.stereotype.Service;

@Service
public class LicenseKeyService {

    private final LicenseKeyRepository licenseKeyRepository;

    public LicenseKeyService(LicenseKeyRepository licenseKeyRepository) {
        this.licenseKeyRepository = licenseKeyRepository;
    }

    public LicenseKeyDTO validateLicenseKey(String licenseKey) {
        // Dummy validation logic
        if (licenseKey == null || licenseKey.isEmpty()) {
            return null;
        }

        // Check if the license key exists in the database
        LicenseKey existingLicenseKey = licenseKeyRepository.findByLicenseKey(licenseKey);
        if (existingLicenseKey != null) {
            // Convert entity to DTO
            LicenseKeyDTO licenseKeyDTO = new LicenseKeyDTO();
            licenseKeyDTO.setCompanyName(existingLicenseKey.getCompanyName());
            licenseKeyDTO.setMachineName(existingLicenseKey.getMachineName());
            licenseKeyDTO.setLicenseType(existingLicenseKey.getLicenseType());
            licenseKeyDTO.setValidity(existingLicenseKey.getValidity());
            licenseKeyDTO.setLicenseKey(existingLicenseKey.getLicenseKey());
            return licenseKeyDTO;
        }

        // Create a dummy LicenseKeyDTO object
        LicenseKeyDTO licenseKeyDTO = new LicenseKeyDTO();
        licenseKeyDTO.setCompanyName("TechCorp");
        licenseKeyDTO.setMachineName("MachineA");
        licenseKeyDTO.setLicenseType("standard");
        licenseKeyDTO.setValidity("2025-12-07");
        licenseKeyDTO.setLicenseKey(licenseKey);

        // Convert DTO to entity
        LicenseKey newLicenseKey = new LicenseKey();
        newLicenseKey.setCompanyName(licenseKeyDTO.getCompanyName());
        newLicenseKey.setMachineName(licenseKeyDTO.getMachineName());
        newLicenseKey.setLicenseType(licenseKeyDTO.getLicenseType());
        newLicenseKey.setValidity(licenseKeyDTO.getValidity());
        newLicenseKey.setLicenseKey(licenseKeyDTO.getLicenseKey());

        // Save the new license key to the database
        licenseKeyRepository.save(newLicenseKey);

        return licenseKeyDTO;
    }
}