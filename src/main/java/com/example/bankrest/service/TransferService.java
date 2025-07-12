package com.example.bankrest.service;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Transfer;
import com.example.bankrest.repository.TransferRepository;
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
     * @throws IllegalArgumentException если карты принадлежат разным пользователям или недостаточно средств
     * @throws IllegalStateException если карты не активны
     */
    @Transactional
    public Transfer transferBetweenCards(Card fromCard, Card toCard, BigDecimal amount) {
        if (!fromCard.getUser().getId().equals(toCard.getUser().getId())) {
            throw new IllegalArgumentException("Transfers allowed only between own cards");
        }
        if (fromCard.getStatus() != com.example.bankrest.entity.CardStatus.ACTIVE ||
            toCard.getStatus() != com.example.bankrest.entity.CardStatus.ACTIVE) {
            throw new IllegalStateException("Both cards must be active");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
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
