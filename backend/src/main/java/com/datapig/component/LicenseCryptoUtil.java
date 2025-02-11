package com.datapig.component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
public class LicenseCryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final String KEY_FILE = "secret.key";

    @Value("${secret.key.path}")
    private String keyFilePath;

    public void generateAndSaveSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256); // AES-256
        SecretKey secretKey = keyGenerator.generateKey();
        saveSecretKey(secretKey);
    }

    // Method to save the secret key to a file
    private static void saveSecretKey(SecretKey secretKey) throws IOException {
        byte[] keyBytes = secretKey.getEncoded();
        try (FileOutputStream fos = new FileOutputStream(KEY_FILE)) {
            fos.write(keyBytes);
        }
    }

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

    // Method to load the secret key from a file
    public SecretKey loadSecretKey() throws IOException {
        System.out.println("Key file path: " + keyFilePath); // Add this line for debugging
        File keyFile = new File(keyFilePath);
        System.out.println("Decrypted string: " + keyFile); // Add this line for debugging
        byte[] keyBytes = new byte[(int) keyFile.length()];
        try (FileInputStream fis = new FileInputStream(keyFile)) {
            fis.read(keyBytes);
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    // Main method for testing
    /*
     * public static void main(String[] args) {
     * LicenseCryptoUtil licenseCryptoUtil = new LicenseCryptoUtil();
     * try {
     * // licenseCryptoUtil.generateAndSaveSecretKey();
     * licenseCryptoUtil.keyFilePath =
     * "/Users/karthik/Documents/datapig/dsutilapp/backend/key/secret.key";
     * SecretKey secretKey = licenseCryptoUtil.loadSecretKey();
     * LicenseData originalData = new
     * LicenseData("dataverse-datapigdemoe-unq3971a4f47cb9ef11b8e46045bd003",
     * "PO123", 365, "Acme Inc.", "Enterprise");
     * String encryptedData = licenseCryptoUtil.encrypt(originalData, secretKey);
     * LicenseData decryptedData = licenseCryptoUtil.decrypt(encryptedData,
     * secretKey);
     * 
     * System.out.println("Original Data: " + originalData);
     * System.out.println("Encrypted Data: " + encryptedData);
     * System.out.println("Decrypted Data: " + decryptedData);
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * 
     * }
     */
}
