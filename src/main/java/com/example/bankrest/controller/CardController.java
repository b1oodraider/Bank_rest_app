package com.example.bankrest.controller;

import com.example.bankrest.entity.*;
import com.example.bankrest.entity.CardOperationHistory;
import com.example.bankrest.service.CardService;
import com.example.bankrest.service.UserService;
import com.example.bankrest.service.TransferService;
import com.example.bankrest.service.CardOperationHistoryService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final UserService userService;
    private final TransferService transferService;
    private final CardOperationHistoryService operationHistoryService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Card>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) throw new IllegalArgumentException("Page number must be 0 or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Card>> getUserCards(
            @AuthenticationPrincipal String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) throw new IllegalArgumentException("Page number must be 0 or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        User user = userService.getUserByUsername(username);
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards = cardService.getCardsByUser(user, pageable);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCard(@RequestBody @Valid CreateCardRequest request) {
        try {
            User user = userService.getUserByUsername(request.getUsername());
            Card card = cardService.createCard(request.getCardNumber(), request.getOwner(), request.getExpiryDate(), user);
            return ResponseEntity.ok(card);
        } catch (com.example.bankrest.exception.UserNotFoundException e) {
            return ResponseEntity.badRequest().body("User not found: " + request.getUsername());
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/{cardId}/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<Transfer>> getCardTransfers(
            @PathVariable Long cardId,
            @AuthenticationPrincipal String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) throw new IllegalArgumentException("Page number must be 0 or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        Pageable pageable = PageRequest.of(page, size);
        Card card = cardService.getCardById(cardId);
        User user = userService.getUserByUsername(username);
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.name().equals("ADMIN"));
        if (!isAdmin && !card.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        Page<Transfer> transfers = transferService.getTransfersFromCard(cardId, pageable);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/{cardId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<CardOperationHistory>> getCardHistory(
            @PathVariable Long cardId,
            @AuthenticationPrincipal String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) throw new IllegalArgumentException("Page number must be 0 or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        Pageable pageable = PageRequest.of(page, size);
        Card card = cardService.getCardById(cardId);
        User user = userService.getUserByUsername(username);
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.name().equals("ADMIN"));
        if (!isAdmin && !card.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        Page<CardOperationHistory> history = operationHistoryService.getCardHistory(card, pageable);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Card ID must be a positive number");
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCard(@PathVariable Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Card ID must be a positive number");
        cardService.activateCard(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Card ID must be a positive number");
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class CreateCardRequest {
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "^\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}$", 
                message = "Card number must be exactly 16 digits with optional spaces (e.g., 1234 5678 9012 3456)")
        private String cardNumber;

        @NotBlank(message = "Owner name is required")
        @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Owner name can only contain letters and spaces")
        private String owner;

        @NotNull(message = "Expiry date is required")
        @Future(message = "Expiry date must be in the future")
        private LocalDate expiryDate;

        @NotBlank(message = "Username is required")
        private String username;
    }
}
