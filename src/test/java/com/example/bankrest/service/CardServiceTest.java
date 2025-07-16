package com.example.bankrest.service;

import com.example.bankrest.entity.*;
import com.example.bankrest.exception.CardNotFoundException;
import com.example.bankrest.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setup() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            // Mock initialization complete
        }
    }

    @Test
    void createCard_encryptsAndMasksNumber() {
        String cardNumber = "1234 5678 9012 3456";
        String encrypted = "encrypted";
        when(encryptionService.encrypt(cardNumber)).thenReturn(encrypted);
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User user = User.builder().id(1L).build();
        Card card = cardService.createCard(cardNumber, "Owner", LocalDate.now().plusYears(1), user);

        assertThat(card.getEncryptedNumber()).isEqualTo(encrypted);
        assertThat(card.getMaskedNumber()).isEqualTo("**** **** **** 3456");
        assertThat(card.getOwner()).isEqualTo("Owner");
        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(card.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(card.getUser()).isEqualTo(user);
    }

    @Test
    void createCard_withInvalidCardNumber_throwsException() {
        User user = User.builder().id(1L).build();
        
        assertThrows(IllegalArgumentException.class, () -> 
            cardService.createCard("1234", "Owner", LocalDate.now().plusYears(1), user));
    }

    @Test
    void createCard_withNullCardNumber_throwsException() {
        User user = User.builder().id(1L).build();
        
        assertThrows(IllegalArgumentException.class, () -> 
            cardService.createCard(null, "Owner", LocalDate.now().plusYears(1), user));
    }

    @Test
    void createCard_withPastExpiryDate_throwsException() {
        User user = User.builder().id(1L).build();
        
        assertThrows(IllegalArgumentException.class, () -> 
            cardService.createCard("1234 5678 9012 3456", "Owner", LocalDate.now().minusDays(1), user));
    }

    @Test
    void createCard_withNullUser_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            cardService.createCard("1234 5678 9012 3456", "Owner", LocalDate.now().plusYears(1), null));
    }

    @Test
    void blockCard_setsStatusBlocked() {
        Card card = Card.builder().id(1L).status(CardStatus.ACTIVE).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cardService.blockCard(1L);

        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_withNonExistentCard_throwsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.blockCard(1L));
    }

    @Test
    void activateCard_setsStatusActive() {
        Card card = Card.builder().id(1L).status(CardStatus.BLOCKED).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cardService.activateCard(1L);

        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository).save(card);
    }

    @Test
    void activateCard_withNonExistentCard_throwsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.activateCard(1L));
    }

    @Test
    void deleteCard_callsRepositoryDelete() {
        doNothing().when(cardRepository).deleteById(1L);

        cardService.deleteCard(1L);

        verify(cardRepository).deleteById(1L);
    }

    @Test
    void getCardById_withExistingCard_returnsCard() {
        Card card = Card.builder().id(1L).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Card result = cardService.getCardById(1L);

        assertThat(result).isEqualTo(card);
    }

    @Test
    void getCardById_withNonExistentCard_throwsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(1L));
    }

    @Test
    void updateCardBalance_updatesCardBalance() {
        Card card = Card.builder().id(1L).balance(BigDecimal.ZERO).build();
        BigDecimal newBalance = new BigDecimal("100.50");
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cardService.updateCardBalance(card, newBalance);

        assertThat(card.getBalance()).isEqualTo(newBalance);
        verify(cardRepository).save(card);
    }

    @Test
    void getCardsByUser_returnsPage() {
        User user = User.builder().id(1L).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> expectedPage = new PageImpl<>(Arrays.asList(
                Card.builder().id(1L).user(user).build()
        ));
        
        when(cardRepository.findByUser(user, pageable)).thenReturn(expectedPage);

        Page<Card> result = cardService.getCardsByUser(user, pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(cardRepository).findByUser(user, pageable);
    }

    @Test
    void findCardById_withExistingCard_returnsCard() {
        Card card = Card.builder().id(1L).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Optional<Card> result = cardService.findCardById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(card);
    }

    @Test
    void findCardById_withNonExistentCard_returnsEmpty() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Card> result = cardService.findCardById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void createCard_withEmptyOwner_throwsException() {
        User user = User.builder().id(1L).build();
        
        assertThrows(IllegalArgumentException.class, () -> 
            cardService.createCard("1234 5678 9012 3456", "", LocalDate.now().plusYears(1), user));
    }

    @Test
    void createCard_withNullExpiryDate_throwsException() {
        User user = User.builder().id(1L).build();
        
        assertThrows(IllegalArgumentException.class, () -> 
            cardService.createCard("1234 5678 9012 3456", "Owner", null, user));
    }
}
