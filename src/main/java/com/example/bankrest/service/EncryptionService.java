package com.example.bankrest.service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
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
     * @throws IllegalArgumentException если секретный ключ некорректен
     */
    public EncryptionService(@Value("${app.encryption.secret}") String secret) {
        if (secret == null || secret.length() != 16) {
            throw new IllegalArgumentException("Encryption secret must be exactly 16 characters long");
        }
        byte[] keyBytes = secret.getBytes();
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Шифрует строку.
     *
     * @param data исходные данные
     * @return зашифрованная строка в Base64
     * @throws IllegalArgumentException если данные равны null
     * @throws RuntimeException если произошла ошибка шифрования
     */
    public String encrypt(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
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
     * @throws IllegalArgumentException если данные равны null
     * @throws RuntimeException если произошла ошибка дешифрования
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null) {
            throw new IllegalArgumentException("Encrypted data cannot be null");
        }
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
