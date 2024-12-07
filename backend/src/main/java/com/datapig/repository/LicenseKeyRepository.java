package com.datapig.repository;

import com.datapig.entity.LicenseKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseKeyRepository extends JpaRepository<LicenseKey, Long> {
    LicenseKey findByLicenseKey(String licenseKey);
}
