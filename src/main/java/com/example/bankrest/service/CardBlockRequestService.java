package com.example.bankrest.service;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.CardBlockRequest;
import com.example.bankrest.entity.CardOperationHistory;
import com.example.bankrest.entity.User;
import com.example.bankrest.repository.CardBlockRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CardBlockRequestService {
    private final CardBlockRequestRepository blockRequestRepository;
    private final CardService cardService;
    private final UserService userService;
    private final CardOperationHistoryService operationHistoryService;

    @Transactional
    public CardBlockRequest createBlockRequest(Long cardId, String username, String reason) {
        User requester = userService.getUserByUsername(username);
        Card card = cardService.getCardById(cardId);
        
        if (!card.getUser().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("You can only request blocking of your own cards");
        }
        
        if (card.getStatus() != com.example.bankrest.entity.CardStatus.ACTIVE) {
            throw new IllegalStateException("Can only request blocking of active cards");
        }
        
        CardBlockRequest request = CardBlockRequest.builder()
                .card(card)
                .requester(requester)
                .reason(reason)
                .status(CardBlockRequest.BlockRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        return blockRequestRepository.save(request);
    }

    public Page<CardBlockRequest> getUserRequests(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return blockRequestRepository.findByRequester(user, pageable);
    }

    public Page<CardBlockRequest> getAllRequests(Pageable pageable) {
        return blockRequestRepository.findAll(pageable);
    }

    public Page<CardBlockRequest> getRequestsByStatus(CardBlockRequest.BlockRequestStatus status, Pageable pageable) {
        return blockRequestRepository.findByStatus(status, pageable);
    }

    @Transactional
    public CardBlockRequest processBlockRequest(Long requestId, String adminUsername, boolean approved, String adminComment) {
        User admin = userService.getUserByUsername(adminUsername);
        CardBlockRequest request = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Block request not found"));
        
        if (request.getStatus() != CardBlockRequest.BlockRequestStatus.PENDING) {
            throw new IllegalStateException("Request has already been processed");
        }
        
        request.setAdmin(admin);
        request.setAdminComment(adminComment);
        request.setProcessedAt(LocalDateTime.now());
        
        if (approved) {
            request.setStatus(CardBlockRequest.BlockRequestStatus.APPROVED);
            operationHistoryService.recordOperation(
                    request.getCard(),
                    CardOperationHistory.OperationType.BLOCK,
                    admin,
                    request.getCard().getStatus(),
                    com.example.bankrest.entity.CardStatus.BLOCKED,
                    "Blocked via user request: " + request.getReason()
            );
        } else {
            request.setStatus(CardBlockRequest.BlockRequestStatus.REJECTED);
        }
        
        return blockRequestRepository.save(request);
    }
} 
