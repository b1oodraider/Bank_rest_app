package com.example.bankrest.repository;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUser(User user, Pageable pageable);
}
