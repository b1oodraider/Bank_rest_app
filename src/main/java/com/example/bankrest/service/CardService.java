package com.example.bankrest.service;

import com.example.bankrest.entity.*;
import com.example.bankrest.repository.CardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Сервис для управления банковскими картами.
 * Обеспечивает создание, блокировку, активацию, удаление и получение карт.
 */
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;

    /**
     * Получить страницы карт пользователя с пагинацией.
     *
     * @param user пользователь
     * @param pageable параметры пагинации
     * @return страница карт
     */
    public Page<Card> getCardsByUser(User user, Pageable pageable) {
        return cardRepository.findByUser(user, pageable);
    }

    /**
     * Получить карту по идентификатору.
     *
     * @param id идентификатор карты
     * @return Optional с картой или пустой, если не найдена
     */
    public Optional<Card> getCardById(Long id) {
        return cardRepository.findById(id);
    }

    /**
     * Создать новую карту с шифрованием номера и маскированием.
     *
     * @param cardNumber номер карты в открытом виде
     * @param owner владелец карты
     * @param expiryDate дата окончания действия
     * @param user владелец (пользователь)
     * @return созданная карта
     */
    @Transactional
    public Card createCard(String cardNumber, String owner, LocalDate expiryDate, User user) {
        String encryptedNumber = encryptionService.encrypt(cardNumber);
        String maskedNumber = maskCardNumber(cardNumber);
        Card card = Card.builder()
                .encryptedNumber(encryptedNumber)
                .maskedNumber(maskedNumber)
                .owner(owner)
                .expiryDate(expiryDate)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();
        return cardRepository.save(card);
    }

    /**
     * Заблокировать карту по идентификатору.
     *
     * @param cardId идентификатор карты
     */
    @Transactional
    public void blockCard(Long cardId) {
        cardRepository.findById(cardId).ifPresent(card -> {
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
        });
    }

    /**
     * Активировать карту по идентификатору.
     *
     * @param cardId идентификатор карты
     */
    @Transactional
    public void activateCard(Long cardId) {
        cardRepository.findById(cardId).ifPresent(card -> {
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
        });
    }

    /**
     * Удалить карту по идентификатору.
     *
     * @param cardId идентификатор карты
     */
    @Transactional
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    /**
     * Маскирует номер карты, оставляя видимыми только последние 4 цифры.
     *
     * @param cardNumber номер карты в открытом виде
     * @return замаскированный номер карты
     */
    private String maskCardNumber(String cardNumber) {
        String digitsOnly = cardNumber.replaceAll("\\D", "");
        if (digitsOnly.length() < 4) {
            return "****";
        }
        String last4 = digitsOnly.substring(digitsOnly.length() - 4);
        return "**** **** **** " + last4;
    }

    /**
     * Обновляет баланс карты.
     *
     * @param card карта
     * @param newBalance новый баланс
     */
    @Transactional
    public void updateCardBalance(Card card, BigDecimal newBalance) {
        card.setBalance(newBalance);
        cardRepository.save(card);
    }
}
