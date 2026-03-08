package com.example.PotteryPotSchool.service.Login.impl;

import com.example.PotteryPotSchool.dto.Login.LoginRequest;
import com.example.PotteryPotSchool.dto.Login.LoginResponse;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.service.Login.AuthService;
import com.example.PotteryPotSchool.service.Login.JwtService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        if (request == null
                || request.getEmail() == null
                || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Objects.equals(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setUser(toUser(user));

        return response;
    }

    private User toUser(UserEntity user) {
        User userDto = new User();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        return userDto;
    }
}
