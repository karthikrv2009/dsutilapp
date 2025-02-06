package com.datapig.component;

public class LicenseData {
    private String environment;
    private String purchaseOrder;
    private int days;

    public LicenseData(String environment, String purchaseOrder, int days) {
        this.environment = environment;
        this.purchaseOrder = purchaseOrder;
        this.days = days;
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
        return environment + "," + purchaseOrder + "," + days;
    }

    public static LicenseData fromString(String data) {
        String[] parts = data.split(",");
        return new LicenseData(parts[0], parts[1], Integer.parseInt(parts[2]));
    }
}
