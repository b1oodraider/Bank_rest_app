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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

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
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (page < 0) {
                throw new IllegalArgumentException("Page number must be 0 or greater");
            }
            if (size < 1 || size > 100) {
                throw new IllegalArgumentException("Page size must be between 1 and 100");
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw e;
        }
    }

    @Data
    public static class CreateUserRequest {
        @NotBlank(message = "Username is required and cannot be empty")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        private String username;
        
        @NotBlank(message = "Password is required and cannot be empty")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        private String password;
        
        @NotEmpty(message = "At least one role is required")
        @Size(min = 1, max = 5, message = "User can have between 1 and 5 roles")
        private Set<Role> roles;
    }
}
