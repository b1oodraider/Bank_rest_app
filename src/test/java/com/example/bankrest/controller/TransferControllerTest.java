package com.example.bankrest.controller;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.CardStatus;
import com.example.bankrest.entity.Transfer;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.CardNotFoundException;
import com.example.bankrest.exception.InsufficientFundsException;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.service.CardService;
import com.example.bankrest.service.TransferService;
import com.example.bankrest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferController Tests")
class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @Mock
    private CardService cardService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransferController transferController;

    private User user;
    private Card fromCard;
    private Card toCard;
    private Transfer transfer;
    private Validator validator;

    @BeforeEach
    void setUp() {
        
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        
        
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .build();
        
        fromCard = Card.builder()
                .id(1L)
                .user(user)
                .encryptedNumber("encrypted1234567890123456")
                .maskedNumber("**** **** **** 1234")
                .owner("Test User")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE)
                .build();
        
        toCard = Card.builder()
                .id(2L)
                .user(user)
                .encryptedNumber("encrypted6543210987654321")
                .maskedNumber("**** **** **** 5678")
                .owner("Test User")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("500.00"))
                .status(CardStatus.ACTIVE)
                .build();
        
        transfer = Transfer.builder()
                .id(1L)
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(new BigDecimal("100.50"))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Successful Transfer Tests")
    class SuccessfulTransferTests {

        @Test
        @DisplayName("Should successfully transfer between cards")
        void transfer_successfulTransfer_returnsTransfer() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("100.50"));

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardById(1L)).thenReturn(fromCard);
            when(cardService.getCardById(2L)).thenReturn(toCard);
            when(transferService.transferBetweenCards(fromCard, toCard, request.getAmount())).thenReturn(transfer);

            
            ResponseEntity<Transfer> response = transferController.transfer("testuser", request);

            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(transfer);
            verify(transferService).transferBetweenCards(fromCard, toCard, request.getAmount());
        }

        @Test
        @DisplayName("Should handle minimum transfer amount")
        void transfer_minimumAmount_success() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("0.01"));

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardById(1L)).thenReturn(fromCard);
            when(cardService.getCardById(2L)).thenReturn(toCard);
            when(transferService.transferBetweenCards(fromCard, toCard, request.getAmount())).thenReturn(transfer);

            
            ResponseEntity<Transfer> response = transferController.transfer("testuser", request);

            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should return forbidden when from card belongs to different user")
        void transfer_fromCardDifferentUser_returnsForbidden() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("100.50"));

            User otherUser = User.builder().id(2L).username("otheruser").build();
            Card otherUserCard = Card.builder().id(1L).user(otherUser).build();

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardById(1L)).thenReturn(otherUserCard);
            when(cardService.getCardById(2L)).thenReturn(toCard);

            
            ResponseEntity<Transfer> response = transferController.transfer("testuser", request);

            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            verify(transferService, never()).transferBetweenCards(any(), any(), any());
        }

        @Test
        @DisplayName("Should return forbidden when to card belongs to different user")
        void transfer_toCardDifferentUser_returnsForbidden() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("100.50"));

            User otherUser = User.builder().id(2L).username("otheruser").build();
            Card otherUserCard = Card.builder().id(2L).user(otherUser).build();

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardById(1L)).thenReturn(fromCard);
            when(cardService.getCardById(2L)).thenReturn(otherUserCard);

            
            ResponseEntity<Transfer> response = transferController.transfer("testuser", request);

            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            verify(transferService, never()).transferBetweenCards(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should propagate UserNotFoundException")
        void transfer_userNotFound_throwsException() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("100.50"));

            when(userService.getUserByUsername("testuser")).thenThrow(new UserNotFoundException("User not found"));

            
            assertThatThrownBy(() -> transferController.transfer("testuser", request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("Should propagate CardNotFoundException")
        void transfer_cardNotFound_throwsException() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("100.50"));

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardById(1L)).thenThrow(new CardNotFoundException("Card not found"));

            
            assertThatThrownBy(() -> transferController.transfer("testuser", request))
                    .isInstanceOf(CardNotFoundException.class)
                    .hasMessage("Card not found");
        }

        @Test
        @DisplayName("Should propagate InsufficientFundsException")
        void transfer_insufficientFunds_throwsException() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("2000.00")); 

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardById(1L)).thenReturn(fromCard);
            when(cardService.getCardById(2L)).thenReturn(toCard);
            when(transferService.transferBetweenCards(fromCard, toCard, request.getAmount()))
                    .thenThrow(new InsufficientFundsException());

            
            assertThatThrownBy(() -> transferController.transfer("testuser", request))
                    .isInstanceOf(InsufficientFundsException.class);
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException")
        void transfer_invalidAmount_throwsException() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("-100.50")); 

            
            assertThatThrownBy(() -> transferController.transfer("testuser", request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Transfer amount must be at least 0.01");
        }
    }

    @Nested
    @DisplayName("TransferRequest Validation Tests")
    class TransferRequestValidationTests {

        @Test
        @DisplayName("Should validate TransferRequest with valid data")
        void transferRequest_validData_noViolations() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("100.50"));

            
            Set<ConstraintViolation<TransferController.TransferRequest>> violations = validator.validate(request);

            
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when fromCardId is null")
        void transferRequest_nullFromCardId_hasViolations() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(null);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("100.50"));

            
            Set<ConstraintViolation<TransferController.TransferRequest>> violations = validator.validate(request);

            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("From card ID is required");
        }

        @Test
        @DisplayName("Should fail validation when toCardId is null")
        void transferRequest_nullToCardId_hasViolations() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(null);
            request.setAmount(new BigDecimal("100.50"));

            
            Set<ConstraintViolation<TransferController.TransferRequest>> violations = validator.validate(request);

            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("To card ID is required");
        }

        @Test
        @DisplayName("Should fail validation when amount is null")
        void transferRequest_nullAmount_hasViolations() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(null);

            
            Set<ConstraintViolation<TransferController.TransferRequest>> violations = validator.validate(request);

            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount is required");
        }

        @Test
        @DisplayName("Should fail validation when amount is zero")
        void transferRequest_zeroAmount_hasViolations() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(BigDecimal.ZERO);

            
            Set<ConstraintViolation<TransferController.TransferRequest>> violations = validator.validate(request);

            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount must be greater than 0");
        }

        @Test
        @DisplayName("Should fail validation when amount is negative")
        void transferRequest_negativeAmount_hasViolations() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("-100.50"));

            
            Set<ConstraintViolation<TransferController.TransferRequest>> violations = validator.validate(request);

            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount must be greater than 0");
        }

        @Test
        @DisplayName("Should pass validation with minimum amount")
        void transferRequest_minimumAmount_noViolations() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("0.01"));

            
            Set<ConstraintViolation<TransferController.TransferRequest>> violations = validator.validate(request);

            
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle same card transfer")
        void transfer_sameCard_throwsException() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(1L); 
            request.setAmount(new BigDecimal("100.50"));

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardById(1L)).thenReturn(fromCard);
            when(transferService.transferBetweenCards(fromCard, fromCard, request.getAmount()))
                    .thenThrow(new IllegalArgumentException("Cannot transfer to same card"));

            
            assertThatThrownBy(() -> transferController.transfer("testuser", request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot transfer to same card");
        }

        @Test
        @DisplayName("Should handle inactive cards")
        void transfer_inactiveCards_throwsException() {
            
            Card inactiveCard = Card.builder()
                    .id(1L)
                    .user(user)
                    .status(CardStatus.BLOCKED)
                    .build();

            TransferController.TransferRequest request = new TransferController.TransferRequest();
            request.setFromCardId(1L);
            request.setToCardId(2L);
            request.setAmount(new BigDecimal("100.50"));

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardById(1L)).thenReturn(inactiveCard);
            when(cardService.getCardById(2L)).thenReturn(toCard);
            when(transferService.transferBetweenCards(inactiveCard, toCard, request.getAmount()))
                    .thenThrow(new IllegalStateException("Both cards must be active"));

            
            assertThatThrownBy(() -> transferController.transfer("testuser", request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Both cards must be active");
        }
    }

    @Nested
    @DisplayName("TransferRequest Class Tests")
    class TransferRequestClassTests {

        @Test
        @DisplayName("Should have proper getters and setters")
        void transferRequest_gettersAndSetters_workCorrectly() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();
            Long fromCardId = 1L;
            Long toCardId = 2L;
            BigDecimal amount = new BigDecimal("100.50");

            
            request.setFromCardId(fromCardId);
            request.setToCardId(toCardId);
            request.setAmount(amount);

            
            assertThat(request.getFromCardId()).isEqualTo(fromCardId);
            assertThat(request.getToCardId()).isEqualTo(toCardId);
            assertThat(request.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("Should handle null values")
        void transferRequest_nullValues_workCorrectly() {
            
            TransferController.TransferRequest request = new TransferController.TransferRequest();

            
            request.setFromCardId(null);
            request.setToCardId(null);
            request.setAmount(null);

            
            assertThat(request.getFromCardId()).isNull();
            assertThat(request.getToCardId()).isNull();
            assertThat(request.getAmount()).isNull();
        }
    }
} 
