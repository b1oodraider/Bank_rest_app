package com.example.bankrest.controller;

import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.security.JwtUtil;
import com.example.bankrest.service.UserService;
import jakarta.validation.Valid;
import lombok.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Контроллер для аутентификации пользователей.
 * Позволяет получить JWT токен по логину и паролю.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    /**
     * Аутентификация пользователя и выдача JWT токена.
     *
     * @param request объект с username и password
     * @return JWT токен
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userService.findByUsername(request.getUsername()).orElseThrow();

        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        String token = jwtUtil.generateToken(user.getUsername(), roles);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Data
    public static class AuthRequest {
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
    }
}
