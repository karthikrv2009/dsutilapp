package com.datapig.component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Component
public class LicenseCryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final String KEY_FILE = "secret.key";
    private String keyFilePath="key/secret.key";

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
        System.out.println("Key file path: " + keyFilePath);  // Debugging: Path to the key file

        // Get the key file as an InputStream from resources (from inside JAR or file system)
        try (InputStream keyInputStream = getClass().getClassLoader().getResourceAsStream(keyFilePath)) {
            if (keyInputStream == null) {
                throw new FileNotFoundException("Key file not found in resources: " + keyFilePath);
            }

            // Read the bytes of the key from the InputStream
            byte[] keyBytes = keyInputStream.readAllBytes();
            System.out.println("Decrypted string (key bytes length): " + keyBytes.length);  // Debugging: Key byte length

            // Return the SecretKey object from the byte array using the algorithm
            return new SecretKeySpec(keyBytes, ALGORITHM);
        }
    }

    // Main method for testing
    
      public static void main(String[] args) {
      LicenseCryptoUtil licenseCryptoUtil = new LicenseCryptoUtil();
      try {
      // licenseCryptoUtil.generateAndSaveSecretKey();
      licenseCryptoUtil.keyFilePath =
      "D:\\DataPig\\dsutilapp\\backend\\key\\secret.key";
      SecretKey secretKey = licenseCryptoUtil.loadSecretKey();
      LicenseData originalData = new
      LicenseData("dataverse-processtest-unq7191fc390b31405f92c74a0436b12",
      "PO123", 365, "Lifetime Products Inc.", "Enterprise");
      String encryptedData = licenseCryptoUtil.encrypt(originalData, secretKey);
      LicenseData decryptedData = licenseCryptoUtil.decrypt(encryptedData,
      secretKey);
      
      System.out.println("Original Data: " + originalData);
      System.out.println("Encrypted Data: " + encryptedData);
      System.out.println("Decrypted Data: " + decryptedData);
      } catch (Exception e) {
      e.printStackTrace();
      }
      
      }
     
}
