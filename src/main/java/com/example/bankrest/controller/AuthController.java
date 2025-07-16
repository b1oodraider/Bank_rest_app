package com.example.bankrest.controller;

import com.example.bankrest.entity.User;
import com.example.bankrest.security.JwtUtil;
import com.example.bankrest.service.UserService;
import jakarta.validation.Valid;
import lombok.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;    
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.*;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userService.getUserByUsername(request.getUsername());

            Set<String> roles = user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
            String token = jwtUtil.generateToken(user.getUsername(), roles);

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            throw e;
        }
    }

    @Data
    public static class AuthRequest {
        @NotBlank(message = "Username is required and cannot be empty")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        private String username;
        
        @NotBlank(message = "Password is required and cannot be empty")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
    }
}
