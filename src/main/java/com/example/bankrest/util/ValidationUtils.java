package com.example.bankrest.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;


public final class ValidationUtils {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}$");
    
    private ValidationUtils() {
        
    }
    
    
    public static void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    
    
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
    

    public static void validateUsername(String username) {
        validateNotNullOrEmpty(username, "Username");
        
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores");
        }
    }
    
    
    public static void validatePassword(String password) {
        validateNotNullOrEmpty(password, "Password");
        
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }
    
    
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
    
    
    public static void validateFutureDate(LocalDate date, String fieldName) {
        validateNotNull(date, fieldName);
        
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " must be in the future");
        }
    }
    
    
    public static void validatePositiveAmount(BigDecimal amount, String fieldName) {
        validateNotNull(amount, fieldName);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }
} 
