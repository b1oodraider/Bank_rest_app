package com.example.bankrest.controller;

import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setup() throws Exception {
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            // Mock initialization complete
        }
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .roles(java.util.Collections.singleton(Role.ROLE_USER))
                .build();
    }

    @Test
    void createUser_successfulCreation_returnsUser() {
        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRoles(java.util.Collections.singleton(Role.ROLE_USER));

        when(userService.createUser("newuser", "password123", java.util.Collections.singleton(Role.ROLE_USER)))
                .thenReturn(testUser);

        ResponseEntity<User> response = userController.createUser(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(testUser);
        verify(userService).createUser("newuser", "password123", java.util.Collections.singleton(Role.ROLE_USER));
    }

    @Test
    void createUser_withAdminRole_returnsUser() {
        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        request.setUsername("adminuser");
        request.setPassword("adminpass");
        request.setRoles(java.util.Collections.singleton(Role.ROLE_ADMIN));

        User adminUser = User.builder()
                .id(2L)
                .username("adminuser")
                .password("encodedPassword")
                .roles(java.util.Collections.singleton(Role.ROLE_ADMIN))
                .build();

        when(userService.createUser("adminuser", "adminpass", java.util.Collections.singleton(Role.ROLE_ADMIN)))
                .thenReturn(adminUser);

        ResponseEntity<User> response = userController.createUser(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(adminUser);
        assertThat(Objects.requireNonNull(response.getBody()).getRoles()).contains(Role.ROLE_ADMIN);
    }

    @Test
    void getUsers_returnsPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser), pageable, 1);

        when(userService.getUsers(pageable)).thenReturn(userPage);

        ResponseEntity<Page<User>> response = userController.getUsers(0, 10);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(testUser);
        verify(userService).getUsers(pageable);
    }

    @Test
    void getUsers_withPagination_returnsCorrectPage() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser), pageable, 1);

        when(userService.getUsers(pageable)).thenReturn(userPage);

        ResponseEntity<Page<User>> response = userController.getUsers(1, 5);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNumber()).isEqualTo(1);
        assertThat(response.getBody().getSize()).isEqualTo(5);
    }

    @Test
    void createUserRequest_validation() {
        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        
        
        assertThat(request.getUsername()).isNull();
        assertThat(request.getPassword()).isNull();
        assertThat(request.getRoles()).isNull();
        
        
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setRoles(java.util.Collections.singleton(Role.ROLE_USER));
        
        assertThat(request.getUsername()).isEqualTo("testuser");
        assertThat(request.getPassword()).isEqualTo("password123");
        assertThat(request.getRoles()).contains(Role.ROLE_USER);
    }

    @Test
    void createUser_withInvalidRequest_throwsException() {
        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        request.setUsername(""); 
        request.setPassword("123"); 
        request.setRoles(java.util.Collections.singleton(Role.ROLE_USER));

        when(userService.createUser(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid user data"));

        assertThatThrownBy(() -> userController.createUser(request))
                .isInstanceOf(Exception.class);
    }
} 
