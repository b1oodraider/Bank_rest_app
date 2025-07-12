package com.example.bankrest.controller;

import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.security.JwtUtil;
import com.example.bankrest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_success_returnsToken() {
        AuthController.AuthRequest request = new AuthController.AuthRequest();
        request.setUsername("user");
        request.setPassword("pass");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        User user = User.builder()
                .username("user")
                .roles(Set.of(Role.ROLE_USER))
                .build();
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(eq("user"), anySet())).thenReturn("token");

        ResponseEntity<?> response = authController.login(request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(AuthController.AuthResponse.class);
        AuthController.AuthResponse authResponse = (AuthController.AuthResponse) response.getBody();
        assertThat(authResponse.getToken()).isEqualTo("token");
    }
}
