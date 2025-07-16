package com.example.bankrest.service;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.CardOperationHistory;
import com.example.bankrest.entity.CardStatus;
import com.example.bankrest.entity.User;
import com.example.bankrest.repository.CardOperationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CardOperationHistoryService {
    private final CardOperationHistoryRepository operationHistoryRepository;


    @Transactional
    public CardOperationHistory recordOperation(Card card, CardOperationHistory.OperationType operationType, 
                                               User performedBy, CardStatus previousStatus, 
                                               CardStatus newStatus, String comment) {
        CardOperationHistory history = CardOperationHistory.builder()
                .card(card)
                .operationType(operationType)
                .performedBy(performedBy)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        
        return operationHistoryRepository.save(history);
    }

    public Page<CardOperationHistory> getCardHistory(Card card, Pageable pageable) {
        return operationHistoryRepository.findByCardOrderByCreatedAtDesc(card, pageable);
    }


    public Page<CardOperationHistory> getCardHistoryByType(Card card, CardOperationHistory.OperationType operationType, Pageable pageable) {
        return operationHistoryRepository.findByCardAndOperationTypeOrderByCreatedAtDesc(card, operationType, pageable);
    }
} 
