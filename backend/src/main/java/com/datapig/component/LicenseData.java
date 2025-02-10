package com.datapig.component;

public class LicenseData {
    private String environment;
    private String purchaseOrder;
    private int days;
    private String companyName;
    private String licenseType;

    public LicenseData() {
    }

    public LicenseData(String environment, String purchaseOrder, int days, String companyName, String licenseType) {
        this.environment = environment;
        this.purchaseOrder = purchaseOrder;
        this.days = days;
        this.companyName = companyName;
        this.licenseType = licenseType;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(String purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return environment + "," + purchaseOrder + "," + days + "," + companyName + "," + licenseType;
    }

    public static LicenseData fromString(String data) {
        String[] parts = data.split(",");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid data format: " + data);
        }
        LicenseData licenseData = new LicenseData();
        licenseData.setEnvironment(parts[0]);
        licenseData.setPurchaseOrder(parts[1]);
        licenseData.setDays(Integer.parseInt(parts[2]));
        licenseData.setCompanyName(parts[3]);
        licenseData.setLicenseType(parts[4]);
        licenseData.toString();
        return licenseData;
    }

}
