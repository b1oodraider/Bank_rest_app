package com.example.bankrest.service;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Transfer;
import com.example.bankrest.exception.InsufficientFundsException;
import com.example.bankrest.repository.TransferRepository;
import com.example.bankrest.util.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сервис для выполнения переводов между картами пользователя.
 */
@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final CardService cardService;

    /**
     * Выполняет перевод между двумя картами одного пользователя.
     *
     * @param fromCard карта-отправитель
     * @param toCard карта-получатель
     * @param amount сумма перевода
     * @return объект перевода
     * @throws IllegalArgumentException если параметры некорректны или карты принадлежат разным пользователям
     * @throws IllegalStateException если карты не активны
     * @throws InsufficientFundsException если недостаточно средств
     */
    @Transactional
    public Transfer transferBetweenCards(Card fromCard, Card toCard, BigDecimal amount) {
        // Validate input parameters using utility methods
        ValidationUtils.validateNotNull(fromCard, "From card");
        ValidationUtils.validateNotNull(toCard, "To card");
        ValidationUtils.validatePositiveAmount(amount, "Amount");
        
        // Validate cards belong to the same user
        if (!fromCard.getUser().getId().equals(toCard.getUser().getId())) {
            throw new IllegalArgumentException("Transfers allowed only between own cards");
        }
        
        // Validate cards are active
        if (fromCard.getStatus() != com.example.bankrest.entity.CardStatus.ACTIVE ||
            toCard.getStatus() != com.example.bankrest.entity.CardStatus.ACTIVE) {
            throw new IllegalStateException("Both cards must be active");
        }
        
        // Validate sufficient funds
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        cardService.updateCardBalance(fromCard, fromCard.getBalance().subtract(amount));
        cardService.updateCardBalance(toCard, toCard.getBalance().add(amount));

        Transfer transfer = Transfer.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();

        return transferRepository.save(transfer);
    }
}
