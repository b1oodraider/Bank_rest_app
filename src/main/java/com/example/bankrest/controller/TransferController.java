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
        try {
            User user = userService.getUserByUsername(username);

            Card fromCard = cardService.getCardById(request.getFromCardId());
            Card toCard = cardService.getCardById(request.getToCardId());

            if (!fromCard.getUser().getId().equals(user.getId()) || !toCard.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).build();
            }

            Transfer transfer = transferService.transferBetweenCards(fromCard, toCard, request.getAmount());
            return ResponseEntity.ok(transfer);
        } catch (Exception e) {
            // GlobalExceptionHandler will handle specific exceptions
            throw e;
        }
    }

    @Data
    public static class TransferRequest {
        @jakarta.validation.constraints.NotNull(message = "From card ID is required")
        private Long fromCardId;
        
        @jakarta.validation.constraints.NotNull(message = "To card ID is required")
        private Long toCardId;
        
        @jakarta.validation.constraints.NotNull(message = "Amount is required")
        @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
    }
}
