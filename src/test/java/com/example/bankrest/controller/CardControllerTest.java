package com.example.bankrest.controller;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.CardStatus;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.CardNotFoundException;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.service.CardService;
import com.example.bankrest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardController Tests")
class CardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CardController cardController;

    private User user;
    private Card card;
    private Validator validator;

    @BeforeEach
    void setUp() {
        // Setup validator for validation tests
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Setup test data
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .build();
        
        card = Card.builder()
                .id(1L)
                .user(user)
                .encryptedNumber("encrypted1234567890123456")
                .maskedNumber("**** **** **** 1234")
                .owner("Test User")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Get User Cards Tests")
    class GetUserCardsTests {

        @Test
        @DisplayName("Should return user cards with pagination")
        void getUserCards_returnsPage() {
            // Given
            Page<Card> page = new PageImpl<>(Arrays.asList(card));
            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardsByUser(eq(user), any(Pageable.class))).thenReturn(page);

            // When
            ResponseEntity<Page<Card>> response = cardController.getUserCards("testuser", 0, 10);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getOwner()).isEqualTo("Test User");
            verify(cardService).getCardsByUser(user, PageRequest.of(0, 10));
        }

        @Test
        @DisplayName("Should return empty page when user has no cards")
        void getUserCards_noCards_returnsEmptyPage() {
            // Given
            Page<Card> emptyPage = new PageImpl<>(Collections.emptyList());
            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardsByUser(eq(user), any(Pageable.class))).thenReturn(emptyPage);

            // When
            ResponseEntity<Page<Card>> response = cardController.getUserCards("testuser", 0, 10);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle custom pagination parameters")
        void getUserCards_customPagination_usesCorrectParameters() {
            // Given
            Page<Card> page = new PageImpl<>(Arrays.asList(card));
            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardsByUser(eq(user), any(Pageable.class))).thenReturn(page);

            // When
            ResponseEntity<Page<Card>> response = cardController.getUserCards("testuser", 2, 5);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cardService).getCardsByUser(user, PageRequest.of(2, 5));
        }

        @Test
        @DisplayName("Should propagate UserNotFoundException")
        void getUserCards_userNotFound_throwsException() {
            // Given
            when(userService.getUserByUsername("testuser")).thenThrow(new UserNotFoundException("User not found"));

            // When & Then
            assertThatThrownBy(() -> cardController.getUserCards("testuser", 0, 10))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("Create Card Tests")
    class CreateCardTests {

        @Test
        @DisplayName("Should successfully create card")
        void createCard_successfulCreation_returnsCard() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.createCard(request.getCardNumber(), request.getOwner(), request.getExpiryDate(), user))
                    .thenReturn(card);

            // When
            ResponseEntity<Card> response = cardController.createCard("testuser", request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(card);
            verify(cardService).createCard(request.getCardNumber(), request.getOwner(), request.getExpiryDate(), user);
        }

        @Test
        @DisplayName("Should handle card number with spaces")
        void createCard_cardNumberWithSpaces_success() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234 5678 9012 3456");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.createCard(request.getCardNumber(), request.getOwner(), request.getExpiryDate(), user))
                    .thenReturn(card);

            // When
            ResponseEntity<Card> response = cardController.createCard("testuser", request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should propagate UserNotFoundException")
        void createCard_userNotFound_throwsException() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            when(userService.getUserByUsername("testuser")).thenThrow(new UserNotFoundException("User not found"));

            // When & Then
            assertThatThrownBy(() -> cardController.createCard("testuser", request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException from service")
        void createCard_invalidCardNumber_throwsException() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.createCard(request.getCardNumber(), request.getOwner(), request.getExpiryDate(), user))
                    .thenThrow(new IllegalArgumentException("Invalid card number"));

            // When & Then
            assertThatThrownBy(() -> cardController.createCard("testuser", request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid card number");
        }
    }

    @Nested
    @DisplayName("Block Card Tests")
    class BlockCardTests {

        @Test
        @DisplayName("Should successfully block card")
        void blockCard_successfulBlock_returnsOk() {
            // Given
            doNothing().when(cardService).blockCard(1L);

            // When
            ResponseEntity<Void> response = cardController.blockCard(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cardService).blockCard(1L);
        }

        @Test
        @DisplayName("Should propagate CardNotFoundException")
        void blockCard_cardNotFound_throwsException() {
            // Given
            doThrow(new CardNotFoundException(1L)).when(cardService).blockCard(1L);

            // When & Then
            assertThatThrownBy(() -> cardController.blockCard(1L))
                    .isInstanceOf(CardNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Activate Card Tests")
    class ActivateCardTests {

        @Test
        @DisplayName("Should successfully activate card")
        void activateCard_successfulActivation_returnsOk() {
            // Given
            doNothing().when(cardService).activateCard(1L);

            // When
            ResponseEntity<Void> response = cardController.activateCard(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cardService).activateCard(1L);
        }

        @Test
        @DisplayName("Should propagate CardNotFoundException")
        void activateCard_cardNotFound_throwsException() {
            // Given
            doThrow(new CardNotFoundException(1L)).when(cardService).activateCard(1L);

            // When & Then
            assertThatThrownBy(() -> cardController.activateCard(1L))
                    .isInstanceOf(CardNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Card Tests")
    class DeleteCardTests {

        @Test
        @DisplayName("Should successfully delete card")
        void deleteCard_successfulDeletion_returnsOk() {
            // Given
            doNothing().when(cardService).deleteCard(1L);

            // When
            ResponseEntity<Void> response = cardController.deleteCard(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cardService).deleteCard(1L);
        }

        @Test
        @DisplayName("Should handle non-existent card deletion gracefully")
        void deleteCard_nonExistentCard_returnsOk() {
            // Given
            doNothing().when(cardService).deleteCard(999L);

            // When
            ResponseEntity<Void> response = cardController.deleteCard(999L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cardService).deleteCard(999L);
        }
    }

    @Nested
    @DisplayName("CreateCardRequest Validation Tests")
    class CreateCardRequestValidationTests {

        @Test
        @DisplayName("Should validate CreateCardRequest with valid data")
        void createCardRequest_validData_noViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should validate card number with spaces")
        void createCardRequest_cardNumberWithSpaces_noViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234 5678 9012 3456");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when card number is null")
        void createCardRequest_nullCardNumber_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber(null);
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Card number is required");
        }

        @Test
        @DisplayName("Should fail validation when card number is blank")
        void createCardRequest_blankCardNumber_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("   ");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(2);
            assertThat(violations).anyMatch(v -> v.getMessage().equals("Card number is required"));
            assertThat(violations).anyMatch(v -> v.getMessage().equals("Card number must be 16 digits with optional spaces"));
        }

        @Test
        @DisplayName("Should fail validation when card number format is invalid")
        void createCardRequest_invalidCardNumberFormat_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("123456789012345"); // 15 digits
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Card number must be 16 digits with optional spaces");
        }

        @Test
        @DisplayName("Should fail validation when card number contains letters")
        void createCardRequest_cardNumberWithLetters_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("123456789012345a");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Card number must be 16 digits with optional spaces");
        }

        @Test
        @DisplayName("Should fail validation when owner is null")
        void createCardRequest_nullOwner_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner(null);
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Owner name is required");
        }

        @Test
        @DisplayName("Should fail validation when owner is blank")
        void createCardRequest_blankOwner_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("   ");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Owner name is required");
        }

        @Test
        @DisplayName("Should fail validation when owner is too short")
        void createCardRequest_shortOwner_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("A");
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Owner name must be between 2 and 100 characters");
        }

        @Test
        @DisplayName("Should fail validation when owner is too long")
        void createCardRequest_longOwner_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner(String.join("", Collections.nCopies(101, "A"))); // 101 characters
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Owner name must be between 2 and 100 characters");
        }

        @Test
        @DisplayName("Should fail validation when expiry date is null")
        void createCardRequest_nullExpiryDate_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("Test User");
            request.setExpiryDate(null);

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Expiry date is required");
        }

        @Test
        @DisplayName("Should fail validation when expiry date is in the past")
        void createCardRequest_pastExpiryDate_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now().minusDays(1));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Expiry date must be in the future");
        }

        @Test
        @DisplayName("Should fail validation when expiry date is today")
        void createCardRequest_todayExpiryDate_hasViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("Test User");
            request.setExpiryDate(LocalDate.now());

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Expiry date must be in the future");
        }

        @Test
        @DisplayName("Should pass validation with minimum valid owner name")
        void createCardRequest_minimumOwnerName_noViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner("AB"); // 2 characters
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with maximum valid owner name")
        void createCardRequest_maximumOwnerName_noViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber("1234567890123456");
            request.setOwner(String.join("", Collections.nCopies(100, "A"))); // 100 characters
            request.setExpiryDate(LocalDate.now().plusYears(2));

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("CreateCardRequest Class Tests")
    class CreateCardRequestClassTests {

        @Test
        @DisplayName("Should have proper getters and setters")
        void createCardRequest_gettersAndSetters_workCorrectly() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            String cardNumber = "1234567890123456";
            String owner = "Test User";
            LocalDate expiryDate = LocalDate.now().plusYears(2);

            // When
            request.setCardNumber(cardNumber);
            request.setOwner(owner);
            request.setExpiryDate(expiryDate);

            // Then
            assertThat(request.getCardNumber()).isEqualTo(cardNumber);
            assertThat(request.getOwner()).isEqualTo(owner);
            assertThat(request.getExpiryDate()).isEqualTo(expiryDate);
        }

        @Test
        @DisplayName("Should handle null values")
        void createCardRequest_nullValues_workCorrectly() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();

            // When
            request.setCardNumber(null);
            request.setOwner(null);
            request.setExpiryDate(null);

            // Then
            assertThat(request.getCardNumber()).isNull();
            assertThat(request.getOwner()).isNull();
            assertThat(request.getExpiryDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle multiple validation violations")
        void createCardRequest_multipleViolations_hasAllViolations() {
            // Given
            CardController.CreateCardRequest request = new CardController.CreateCardRequest();
            request.setCardNumber(null);
            request.setOwner(null);
            request.setExpiryDate(null);

            // When
            Set<ConstraintViolation<CardController.CreateCardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(3);
            assertThat(violations).anyMatch(v -> v.getMessage().equals("Card number is required"));
            assertThat(violations).anyMatch(v -> v.getMessage().equals("Owner name is required"));
            assertThat(violations).anyMatch(v -> v.getMessage().equals("Expiry date is required"));
        }

        @Test
        @DisplayName("Should handle pagination with zero size")
        void getUserCards_zeroSize_usesDefaultPagination() {
            // Given
            Page<Card> page = new PageImpl<>(Collections.emptyList());
            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardsByUser(eq(user), any(Pageable.class))).thenReturn(page);

            // When
            ResponseEntity<Page<Card>> response = cardController.getUserCards("testuser", 0, 0);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cardService).getCardsByUser(user, PageRequest.of(0, 10)); // size should be sanitized to 10
        }

        @Test
        @DisplayName("Should handle negative page number")
        void getUserCards_negativePage_usesCorrectPagination() {
            // Given
            Page<Card> page = new PageImpl<>(Collections.emptyList());
            when(userService.getUserByUsername("testuser")).thenReturn(user);
            when(cardService.getCardsByUser(eq(user), any(Pageable.class))).thenReturn(page);

            // When
            ResponseEntity<Page<Card>> response = cardController.getUserCards("testuser", -1, 10);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cardService).getCardsByUser(user, PageRequest.of(0, 10)); // page should be sanitized to 0
        }
    }
}
