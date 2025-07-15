package com.example.bankrest.service;

import com.example.bankrest.entity.User;
import com.example.bankrest.exception.UserNotFoundException;
import com.example.bankrest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * Сервис для управления пользователями.
 * Предоставляет методы для поиска, создания и управления пользователями.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Поиск пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return Optional с найденным пользователем или пустой, если не найден
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Получить пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    /**
     * Создает нового пользователя с заданными ролями и зашифрованным паролем.
     *
     * @param username имя пользователя
     * @param rawPassword пароль в открытом виде
     * @param roles набор ролей пользователя
     * @return созданный пользователь
     */
    public User createUser(String username, String rawPassword, Set<com.example.bankrest.entity.Role> roles) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .build();
        return userRepository.save(user);
    }

    /**
     * Получить страницы пользователей с пагинацией (только для администраторов).
     *
     * @param pageable параметры пагинации
     * @return страница пользователей
     */
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Получить пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }
}
