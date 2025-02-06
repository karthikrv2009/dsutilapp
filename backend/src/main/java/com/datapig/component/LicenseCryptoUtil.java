package com.datapig.component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class LicenseCryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    // Method to generate a secret key
    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256); // AES-256
        return keyGenerator.generateKey();
    }

    // Method to encrypt data
    public static String encrypt(LicenseData data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.toString().getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Method to decrypt data
    public static LicenseData decrypt(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        String decryptedString = new String(decryptedBytes);
        return LicenseData.fromString(decryptedString);
    }

    // Main method for testing
    public static void main(String[] args) {
        try {
            SecretKey secretKey = generateSecretKey();
            LicenseData originalData = new LicenseData("abcd@12345", "PO12345", 365);
            String encryptedData = encrypt(originalData, secretKey);
            LicenseData decryptedData = decrypt(encryptedData, secretKey);

            System.out.println("Original Data: " + originalData);
            System.out.println("Encrypted Data: " + encryptedData);
            System.out.println("Decrypted Data: " + decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}