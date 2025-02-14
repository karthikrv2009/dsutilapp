package com.datapig.service;

import com.datapig.component.LicenseCryptoUtil;
import com.datapig.component.LicenseData;
import com.datapig.entity.LicenseKey;
import com.datapig.repository.LicenseKeyRepository;

import java.time.LocalDateTime;

import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LicenseKeyService {

    private final LicenseKeyRepository licenseKeyRepository;

    @Autowired
    private LicenseCryptoUtil licenseCryptoUtil;

    public LicenseKeyService(LicenseKeyRepository licenseKeyRepository) {
        this.licenseKeyRepository = licenseKeyRepository;
    }

    public LicenseKey saveLicenseKey(LicenseKey licenseKey) {

        try {

            SecretKey secretKey = licenseCryptoUtil.loadSecretKey();
            LicenseData licenseData = licenseCryptoUtil.decrypt(licenseKey.getLicenseKey(), secretKey);
            licenseKey.setCompanyName(licenseData.getCompanyName());
            licenseKey.setLicenseType(licenseData.getLicenseType());
            licenseKey.setValidity(licenseData.getDays());
            licenseKey.setStartDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
            licenseKey.setEnvironment(licenseData.getEnvironment());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return licenseKeyRepository.save(licenseKey);
    }

    public LicenseKey getLicenseKeyById(Long id) {
        return licenseKeyRepository.findById(id).orElse(null);
    }

    public List<LicenseKey> getAllLicenseKeys() {
        return licenseKeyRepository.findAll();
    }
}