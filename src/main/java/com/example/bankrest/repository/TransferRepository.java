package com.example.bankrest.repository;

import com.example.bankrest.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Page<Transfer> findByFromCard_Id(Long cardId, Pageable pageable);
    Page<Transfer> findByToCard_Id(Long cardId, Pageable pageable);
}
