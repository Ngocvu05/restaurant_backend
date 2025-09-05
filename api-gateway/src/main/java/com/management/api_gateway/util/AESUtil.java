package com.management.api_gateway.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class AESUtil {
    @Value("${app.encryption.secret}")
    private String secret;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    public String encrypt(String plainText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "AES");

        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] encryptedText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Combine IV and encrypted data
        byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedText.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedText, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedText.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    public String decrypt(String encryptedText) throws Exception {
        byte[] decodedText = Base64.getDecoder().decode(encryptedText);

        // Extract IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedText, 0, iv, 0, GCM_IV_LENGTH);

        // Extract encrypted data
        byte[] encrypted = new byte[decodedText.length - GCM_IV_LENGTH];
        System.arraycopy(decodedText, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] decryptedText = cipher.doFinal(encrypted);
        return new String(decryptedText, StandardCharsets.UTF_8);
    }
}
