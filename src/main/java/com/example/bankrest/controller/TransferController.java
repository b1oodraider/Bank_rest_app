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
            @AuthenticationPrincipal(expression = "username") String username,
            @RequestBody @Valid TransferRequest request) {
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
    }
}
