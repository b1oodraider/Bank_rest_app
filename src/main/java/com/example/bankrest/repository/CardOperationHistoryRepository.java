package com.example.bankrest.repository;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.CardOperationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardOperationHistoryRepository extends JpaRepository<CardOperationHistory, Long> {
    Page<CardOperationHistory> findByCardOrderByCreatedAtDesc(Card card, Pageable pageable);
    Page<CardOperationHistory> findByCardAndOperationTypeOrderByCreatedAtDesc(Card card, CardOperationHistory.OperationType operationType, Pageable pageable);
} 
