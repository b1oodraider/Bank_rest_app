package com.example.bankrest.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Утилиты для валидации данных.
 */
public final class ValidationUtils {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}$");
    
    private ValidationUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Проверяет, что строка не null и не пустая.
     *
     * @param value проверяемое значение
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если значение null или пустое
     */
    public static void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    
    /**
     * Проверяет, что объект не null.
     *
     * @param value проверяемое значение
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если значение null
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
    
    /**
     * Проверяет формат имени пользователя.
     *
     * @param username имя пользователя
     * @throws IllegalArgumentException если формат некорректен
     */
    public static void validateUsername(String username) {
        validateNotNullOrEmpty(username, "Username");
        
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores");
        }
    }
    
    /**
     * Проверяет длину пароля.
     *
     * @param password пароль
     * @throws IllegalArgumentException если пароль слишком короткий
     */
    public static void validatePassword(String password) {
        validateNotNullOrEmpty(password, "Password");
        
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }
    
    /**
     * Проверяет формат номера карты.
     *
     * @param cardNumber номер карты
     * @throws IllegalArgumentException если формат некорректен
     */
    public static void validateCardNumber(String cardNumber) {
        validateNotNullOrEmpty(cardNumber, "Card number");
        
        if (!CARD_NUMBER_PATTERN.matcher(cardNumber).matches()) {
            throw new IllegalArgumentException("Card number must be 16 digits with optional spaces");
        }
        
        String digitsOnly = cardNumber.replaceAll("\\D", "");
        if (digitsOnly.length() != 16) {
            throw new IllegalArgumentException("Card number must contain exactly 16 digits");
        }
    }
    
    /**
     * Проверяет, что дата в будущем.
     *
     * @param date проверяемая дата
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если дата в прошлом
     */
    public static void validateFutureDate(LocalDate date, String fieldName) {
        validateNotNull(date, fieldName);
        
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " must be in the future");
        }
    }
    
    /**
     * Проверяет, что сумма положительная.
     *
     * @param amount сумма
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если сумма не положительная
     */
    public static void validatePositiveAmount(BigDecimal amount, String fieldName) {
        validateNotNull(amount, fieldName);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }
} 