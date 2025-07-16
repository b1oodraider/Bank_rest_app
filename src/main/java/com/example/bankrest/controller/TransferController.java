package com.example.bankrest.controller;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Transfer;
import com.example.bankrest.entity.User;
import com.example.bankrest.service.CardService;
import com.example.bankrest.service.TransferService;
import com.example.bankrest.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final CardService cardService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Transfer> transfer(
            @AuthenticationPrincipal String username,
            @RequestBody @Valid TransferRequest request) {
        request.validate();
        User user = userService.getUserByUsername(username);
        Card fromCard = cardService.getCardById(request.getFromCardId());
        Card toCard = cardService.getCardById(request.getToCardId());
        if (!fromCard.getUser().getId().equals(user.getId()) || !toCard.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        Transfer transfer = transferService.transferBetweenCards(fromCard, toCard, request.getAmount());
        return ResponseEntity.ok(transfer);
    }

    @Data
    public static class TransferRequest {
        @NotNull(message = "From card ID is required")
        private Long fromCardId;
        
        @NotNull(message = "To card ID is required")
        private Long toCardId;
        
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
        
        public void validate() {
            if (fromCardId == null || fromCardId <= 0) {
                throw new IllegalArgumentException("From card ID must be a positive number");
            }
            if (toCardId == null || toCardId <= 0) {
                throw new IllegalArgumentException("To card ID must be a positive number");
            }
            if (amount == null) {
                throw new IllegalArgumentException("Transfer amount is required");
            }
            if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                throw new IllegalArgumentException("Transfer amount must be at least 0.01");
            }
            if (amount.compareTo(BigDecimal.valueOf(999999.99)) > 0) {
                throw new IllegalArgumentException("Transfer amount cannot exceed 999,999.99");
            }
            String amountStr = amount.toString();
            if (amountStr.contains(".")) {
                String[] parts = amountStr.split("\\.");
                if (parts[0].length() > 6) {
                    throw new IllegalArgumentException("Transfer amount can have maximum 6 digits before decimal");
                }
                if (parts[1].length() > 2) {
                    throw new IllegalArgumentException("Transfer amount can have maximum 2 digits after decimal");
                }
            } else {
                if (amountStr.length() > 6) {
                    throw new IllegalArgumentException("Transfer amount can have maximum 6 digits before decimal");
                }
            }
        }
    }
}
