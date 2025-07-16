package com.example.bankrest.repository;

import com.example.bankrest.entity.CardBlockRequest;
import com.example.bankrest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequest, Long> {
    
    Page<CardBlockRequest> findByRequester(User requester, Pageable pageable);
    
    
    Page<CardBlockRequest> findByStatus(CardBlockRequest.BlockRequestStatus status, Pageable pageable);


}
