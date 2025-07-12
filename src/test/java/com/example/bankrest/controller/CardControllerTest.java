package com.example.bankrest.controller;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.User;
import com.example.bankrest.service.CardService;
import com.example.bankrest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CardController cardController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserCards_returnsPage() {
        User user = User.builder().id(1L).build();
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));

        Card card = Card.builder()
                .id(1L)
                .owner("Owner")
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardService.getCardsByUser(eq(user), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<Card>> response = cardController.getUserCards("user", 0, 10);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getOwner()).isEqualTo("Owner");
    }
}
