package com.example.bankrest.service;

import com.example.bankrest.entity.*;
import com.example.bankrest.exception.CardNotFoundException;
import com.example.bankrest.repository.CardRepository;
import com.example.bankrest.util.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;
    private final CardOperationHistoryService operationHistoryService;

    public Page<Card> getCardsByUser(User user, Pageable pageable) {
        return cardRepository.findByUser(user, pageable);
    }

    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
    }

    public Optional<Card> findCardById(Long id) {
        return cardRepository.findById(id);
    }

    @Transactional
    public Card createCard(String cardNumber, String owner, LocalDate expiryDate, User user) {
        ValidationUtils.validateCardNumber(cardNumber);
        ValidationUtils.validateNotNullOrEmpty(owner, "Owner");
        ValidationUtils.validateNotNull(expiryDate, "Expiry date");
        ValidationUtils.validateNotNull(user, "User");
        ValidationUtils.validateFutureDate(expiryDate, "Expiry date");
        
        String encryptedNumber = encryptionService.encrypt(cardNumber);
        String maskedNumber = maskCardNumber(cardNumber);
        Card card = Card.builder()
                .encryptedNumber(encryptedNumber)
                .maskedNumber(maskedNumber)
                .owner(owner.trim())
                .expiryDate(expiryDate)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();
        Card savedCard = cardRepository.save(card);
        
        operationHistoryService.recordOperation(
                savedCard,
                CardOperationHistory.OperationType.CREATE,
                null,
                null,
                CardStatus.ACTIVE,
                "Card created"
        );
        
        return savedCard;
    }

    @Transactional
    public void blockCard(Long cardId) {
        Card card = getCardById(cardId);
        CardStatus previousStatus = card.getStatus();
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        
        operationHistoryService.recordOperation(
                card,
                CardOperationHistory.OperationType.BLOCK,
                null,
                previousStatus,
                CardStatus.BLOCKED,
                "Card blocked"
        );
    }

    @Transactional
    public void activateCard(Long cardId) {
        Card card = getCardById(cardId);
        CardStatus previousStatus = card.getStatus();
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        
        operationHistoryService.recordOperation(
                card,
                CardOperationHistory.OperationType.ACTIVATE,
                null,
                previousStatus,
                CardStatus.ACTIVE,
                "Card activated"
        );
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = getCardById(cardId);
        CardStatus previousStatus = card.getStatus();
        
        operationHistoryService.recordOperation(
                card,
                CardOperationHistory.OperationType.DELETE,
                null,
                previousStatus,
                null,
                "Card deleted"
        );
        
        cardRepository.deleteById(cardId);
    }

    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    private String maskCardNumber(String cardNumber) {
        String digitsOnly = cardNumber.replaceAll("\\D", "");
        if (digitsOnly.length() < 4) {
            return "****";
        }
        String last4 = digitsOnly.substring(digitsOnly.length() - 4);
        return "**** **** **** " + last4;
    }

    @Transactional
    public void updateCardBalance(Card card, BigDecimal newBalance) {
        card.setBalance(newBalance);
        cardRepository.save(card);
    }
}
