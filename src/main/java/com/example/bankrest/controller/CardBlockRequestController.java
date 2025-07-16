package com.example.bankrest.controller;

import com.example.bankrest.entity.CardBlockRequest;
import com.example.bankrest.service.CardBlockRequestService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/card-block-requests")
@RequiredArgsConstructor
public class CardBlockRequestController {

    private final CardBlockRequestService blockRequestService;

    /**
     * Создать запрос на блокировку карты (для пользователя).
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardBlockRequest> createBlockRequest(
            @AuthenticationPrincipal String username,
            @RequestBody @Valid CreateBlockRequestRequest request) {
        
        if (request.getCardId() == null || request.getCardId() <= 0) {
            throw new IllegalArgumentException("Card ID must be a positive number");
        }
        
        CardBlockRequest blockRequest = blockRequestService.createBlockRequest(
                request.getCardId(), username, request.getReason());
        return ResponseEntity.ok(blockRequest);
    }

    /**
     * Получить запросы пользователя (для пользователя).
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardBlockRequest>> getMyRequests(
            @AuthenticationPrincipal String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (page < 0) throw new IllegalArgumentException("Page number must be 0 or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CardBlockRequest> requests = blockRequestService.getUserRequests(username, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * Получить все запросы (для администратора).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardBlockRequest>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (page < 0) throw new IllegalArgumentException("Page number must be 0 or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CardBlockRequest> requests = blockRequestService.getAllRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * Получить запросы по статусу (для администратора).
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardBlockRequest>> getRequestsByStatus(
            @PathVariable CardBlockRequest.BlockRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (page < 0) throw new IllegalArgumentException("Page number must be 0 or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CardBlockRequest> requests = blockRequestService.getRequestsByStatus(status, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * Обработать запрос на блокировку (для администратора).
     */
    @PostMapping("/{requestId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardBlockRequest> processBlockRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal String username,
            @RequestBody @Valid ProcessBlockRequestRequest request) {
        
        if (requestId == null || requestId <= 0) {
            throw new IllegalArgumentException("Request ID must be a positive number");
        }
        
        CardBlockRequest processedRequest = blockRequestService.processBlockRequest(
                requestId, username, request.isApproved(), request.getAdminComment());
        return ResponseEntity.ok(processedRequest);
    }

    @Data
    public static class CreateBlockRequestRequest {
        @NotNull(message = "Card ID is required")
        private Long cardId;
        
        private String reason;
    }

    @Data
    public static class ProcessBlockRequestRequest {
        @NotNull(message = "Approval decision is required")
        private Boolean approved;
        
        private String adminComment;
        
        public boolean isApproved() {
            return approved != null && approved;
        }
    }
} 
