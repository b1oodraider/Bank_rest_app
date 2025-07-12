package com.example.bankrest.service;

import jakarta.crypto.Cipher;
import jakarta.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Сервис для шифрования и дешифрования данных (например, номеров карт).
 * Использует AES алгоритм.
 */
@Service
public class EncryptionService {

    private final SecretKeySpec secretKey;

    /**
     * Конструктор, инициализирующий секретный ключ для AES.
     *
     * @param secret секретный ключ (строка длиной 16 байт)
     */
    public EncryptionService(@Value("${app.encryption.secret}") String secret) {
        byte[] keyBytes = secret.getBytes();
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Шифрует строку.
     *
     * @param data исходные данные
     * @return зашифрованная строка в Base64
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    /**
     * Дешифрует строку.
     *
     * @param encryptedData зашифрованные данные в Base64
     * @return исходная строка
     */
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
