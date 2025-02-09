package com.datapig.component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class LicenseCryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    // Method to generate a secret key
    public SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256); // AES-256
        return keyGenerator.generateKey();
    }

    // Method to encrypt data
    public String encrypt(LicenseData data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.toString().getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Method to decrypt data
    // Method to decrypt data
    public LicenseData decrypt(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        String decryptedString = new String(decryptedBytes);
        System.out.println("Decrypted string: " + decryptedString); // Add this line for debugging
        return LicenseData.fromString(decryptedString);
    }

    // Main method for testing
    /*
     * public static void main(String[] args) {
     * try {
     * SecretKey secretKey = generateSecretKey();
     * LicenseData originalData = new
     * LicenseData("dataverse-datapigdemoe-unq3971a4f47cb9ef11b8e46045bd003",
     * "PO123", 365, "Acme Inc.", "Enterprise");
     * String encryptedData = encrypt(originalData, secretKey);
     * LicenseData decryptedData = decrypt(encryptedData, secretKey);
     * 
     * System.out.println("Original Data: " + originalData);
     * System.out.println("Encrypted Data: " + encryptedData);
     * System.out.println("Decrypted Data: " + decryptedData);
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * }
     */
}