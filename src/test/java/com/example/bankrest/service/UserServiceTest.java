package com.example.bankrest.service;

import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByUsername_existingUser_returnsUser() {
        User user = User.builder().username("test").build();
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("test");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("test");
    }

    @Test
    void createUser_encodesPasswordAndSavesUser() {
        String rawPassword = "password";
        String encodedPassword = "encoded";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User user = userService.createUser("user1", rawPassword, Set.of(Role.ROLE_USER));

        assertThat(user.getUsername()).isEqualTo("user1");
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
        assertThat(user.getRoles()).contains(Role.ROLE_USER);
        verify(userRepository).save(any());
    }
}
