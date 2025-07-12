package com.example.bankrest.service;

import com.example.bankrest.entity.*;
import com.example.bankrest.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
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
    void blockCard_setsStatusBlocked() {
        Card card = Card.builder().id(1L).status(CardStatus.ACTIVE).build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cardService.blockCard(1L);

        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(card);
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
    void deleteCard_callsRepositoryDelete() {
        doNothing().when(cardRepository).deleteById(1L);

        cardService.deleteCard(1L);

        verify(cardRepository).deleteById(1L);
    }
}
