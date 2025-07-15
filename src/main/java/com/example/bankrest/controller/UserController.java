package com.example.bankrest.controller;

import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.*;
import java.util.Set;

/**
 * Контроллер для управления пользователями (только для администраторов).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * Создать нового пользователя (только для администраторов).
     *
     * @param request данные для создания пользователя
     * @return созданный пользователь
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid CreateUserRequest request) {
        try {
            User user = userService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRoles()
            );
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // GlobalExceptionHandler will handle specific exceptions
            throw e;
        }
    }

    /**
     * Получить список пользователей с пагинацией (только для администраторов).
     *
     * @param page номер страницы
     * @param size размер страницы
     * @return страница пользователей
     */
    @GetMapping
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            // GlobalExceptionHandler will handle specific exceptions
            throw e;
        }
    }

    @Data
    public static class CreateUserRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        private String username;
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        private String password;
        
        @NotEmpty(message = "At least one role is required")
        private Set<Role> roles;
    }
}
