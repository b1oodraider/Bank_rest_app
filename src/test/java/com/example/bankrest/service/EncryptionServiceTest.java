package com.example.bankrest.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EncryptionServiceTest {

    private final EncryptionService encryptionService = new EncryptionService("1234567890123456");

    @Test
    void encryptAndDecrypt_shouldReturnOriginal() {
        String original = "1234 5678 9012 3456";
        String encrypted = encryptionService.encrypt(original);
        assertThat(encrypted).isNotBlank();

        String decrypted = encryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(original);
    }
}
