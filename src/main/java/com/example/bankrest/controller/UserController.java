package com.example.bankrest.controller;

import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.service.UserService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid CreateUserRequest request) {
        User user = userService.createUser(request.getUsername(), request.getPassword(), request.getRoles());
        return ResponseEntity.ok(user);
    }

    @Data
    public static class CreateUserRequest {
        private String username;
        private String password;
        private Set<Role> roles;
    }
}
