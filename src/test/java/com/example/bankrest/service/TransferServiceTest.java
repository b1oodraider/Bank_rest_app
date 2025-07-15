package com.example.bankrest.service;

import com.example.bankrest.entity.*;
import com.example.bankrest.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransferService transferService;

    private User user;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).build();
        fromCard = Card.builder()
                .id(1L)
                .user(user)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000"))
                .build();
        toCard = Card.builder()
                .id(2L)
                .user(user)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("500"))
                .build();
    }

    @Test
    void transferBetweenCards_successfulTransfer() {
        BigDecimal amount = new BigDecimal("200");
        when(transferRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            BigDecimal balance = invocation.getArgument(1);
            card.setBalance(balance);
            return null;
        }).when(cardService).updateCardBalance(any(), any());

        Transfer transfer = transferService.transferBetweenCards(fromCard, toCard, amount);

        assertThat(transfer.getAmount()).isEqualTo(amount);
        assertThat(transfer.getFromCard()).isEqualTo(fromCard);
        assertThat(transfer.getToCard()).isEqualTo(toCard);
        assertThat(fromCard.getBalance()).isEqualTo(new BigDecimal("800"));
        assertThat(toCard.getBalance()).isEqualTo(new BigDecimal("700"));
        verify(transferRepository).save(any());
    }

    @Test
    void transferBetweenCards_throwsIfDifferentUsers() {
        Card otherUserCard = Card.builder().id(3L).user(User.builder().id(2L).build()).build();

        assertThatThrownBy(() -> transferService.transferBetweenCards(fromCard, otherUserCard, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transfers allowed only between own cards");
    }

    @Test
    void transferBetweenCards_throwsIfCardNotActive() {
        fromCard.setStatus(CardStatus.BLOCKED);

        assertThatThrownBy(() -> transferService.transferBetweenCards(fromCard, toCard, BigDecimal.TEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Both cards must be active");
    }

    @Test
    void transferBetweenCards_throwsIfInsufficientFunds() {
        BigDecimal amount = new BigDecimal("2000");

        assertThatThrownBy(() -> transferService.transferBetweenCards(fromCard, toCard, amount))
                .isInstanceOf(com.example.bankrest.exception.InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");
    }
}
