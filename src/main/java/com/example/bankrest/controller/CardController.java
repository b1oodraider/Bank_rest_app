package com.example.bankrest.controller;

import com.example.bankrest.entity.*;
import com.example.bankrest.service.CardService;
import com.example.bankrest.service.UserService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<Card>> getUserCards(
            @AuthenticationPrincipal(expression = "username") String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userService.getUserByUsername(username);
        // Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards = cardService.getCardsByUser(user, pageable);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    public ResponseEntity<Card> createCard(
            @AuthenticationPrincipal(expression = "username") String username,
            @RequestBody @Valid CreateCardRequest request) {
        User user = userService.getUserByUsername(username);
        Card card = cardService.createCard(request.getCardNumber(), request.getOwner(), request.getExpiryDate(), user);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateCard(@PathVariable Long id) {
        cardService.activateCard(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class CreateCardRequest {
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "^\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}$", message = "Card number must be 16 digits with optional spaces")
        private String cardNumber;

        @NotBlank(message = "Owner name is required")
        @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
        private String owner;

        @NotNull(message = "Expiry date is required")
        @Future(message = "Expiry date must be in the future")
        private LocalDate expiryDate;
    }
}
