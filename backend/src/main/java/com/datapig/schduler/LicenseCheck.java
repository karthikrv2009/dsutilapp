package com.datapig.schduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.datapig.component.LicenseCryptoUtil;
import com.datapig.component.LicenseData;
import com.datapig.entity.LicenseKey;
import com.datapig.service.LicenseKeyService;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class LicenseCheck {

    private static final Logger logger = LoggerFactory.getLogger(LicenseCheck.class);

    @Autowired
    private LicenseKeyService licenseKeyService;

    @Autowired
    private LicenseCryptoUtil LicenseCryptoUtil;

    @Scheduled(fixedRate = 14400000) // 4 hours in milliseconds
    public void checkLicense() {
        try {
            List<LicenseKey> licenseKeys = licenseKeyService.getAllLicenseKeys();
            SecretKey secretKey = LicenseCryptoUtil.generateSecretKey();

            for (LicenseKey licenseKey : licenseKeys) {
                LicenseData licenseData = LicenseCryptoUtil.decrypt(licenseKey.getLicenseKey(), secretKey);
                LocalDate startDate = licenseKey.getStartDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
                LocalDate currentDate = LocalDate.now();
                long daysBetween = ChronoUnit.DAYS.between(startDate, currentDate);

                if (daysBetween > licenseData.getDays()) {
                    LocalDate expiryDate = startDate.plusDays(licenseData.getDays());
                    logger.error("License expired for {}. Expired on {}", licenseKey.getLicenseKey(), expiryDate);
                    System.exit(1); // Terminate the application
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
