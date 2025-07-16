package com.example.bankrest.service;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Transfer;
import com.example.bankrest.exception.InsufficientFundsException;
import com.example.bankrest.repository.TransferRepository;
import com.example.bankrest.util.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final CardService cardService;

    @Transactional
    public Transfer transferBetweenCards(Card fromCard, Card toCard, BigDecimal amount) {
        ValidationUtils.validateNotNull(fromCard, "From card");
        ValidationUtils.validateNotNull(toCard, "To card");
        ValidationUtils.validatePositiveAmount(amount, "Amount");
        
        if (!fromCard.getUser().getId().equals(toCard.getUser().getId())) {
            throw new IllegalArgumentException("Transfers allowed only between own cards");
        }
        
        if (fromCard.getStatus() != com.example.bankrest.entity.CardStatus.ACTIVE ||
            toCard.getStatus() != com.example.bankrest.entity.CardStatus.ACTIVE) {
            throw new IllegalStateException("Both cards must be active");
        }
        
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

    public Page<Transfer> getTransfersFromCard(Long cardId, Pageable pageable) {
        return transferRepository.findByFromCard_Id(cardId, pageable);
    }

    public Page<Transfer> getTransfersToCard(Long cardId, Pageable pageable) {
        return transferRepository.findByToCard_Id(cardId, pageable);
    }
}
