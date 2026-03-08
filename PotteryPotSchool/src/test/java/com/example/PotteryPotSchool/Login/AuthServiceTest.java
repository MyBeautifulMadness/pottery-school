package com.example.PotteryPotSchool.Login;


import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.model.LoginRequest;
import com.example.model.LoginResponse;
import com.example.model.User;
import com.example.service.AuthService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success_returnsTokenAndUser() {

        LoginRequest request = new LoginRequest();
        request.setEmail("student@test.com");
        request.setPassword("password");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("student@test.com");
        user.setPassword("password");
        user.setRole("STUDENT");

        Mockito.when(userRepository.findByEmail("student@test.com"))
                .thenReturn(Optional.of(user));

        Mockito.when(jwtService.generateToken(user))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("student@test.com", response.getUser().getEmail());
        assertEquals("STUDENT", response.getUser().getRole());
    }

    @Test
    void login_emptyBody_throwsException() {

        LoginRequest request = new LoginRequest();

        assertThrows(IllegalArgumentException.class, () ->
                authService.login(request)
        );
    }

    @Test
    void login_missingEmail_throwsException() {

        LoginRequest request = new LoginRequest();
        request.setPassword("password");

        assertThrows(IllegalArgumentException.class, () ->
                authService.login(request)
        );
    }

    @Test
    void login_missingPassword_throwsException() {

        LoginRequest request = new LoginRequest();
        request.setEmail("student@test.com");

        assertThrows(IllegalArgumentException.class, () ->
                authService.login(request)
        );
    }

    @Test
    void login_userNotFound_throwsException() {

        LoginRequest request = new LoginRequest();
        request.setEmail("student@test.com");
        request.setPassword("password");

        Mockito.when(userRepository.findByEmail("student@test.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void login_invalidPassword_throwsException() {

        LoginRequest request = new LoginRequest();
        request.setEmail("student@test.com");
        request.setPassword("wrongPassword");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("student@test.com");
        user.setPassword("encodedPassword");
        user.setRole("STUDENT");

        Mockito.when(userRepository.findByEmail("student@test.com"))
                .thenReturn(Optional.of(user));

        Mockito.when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Invalid password", exception.getMessage());
    }
}

