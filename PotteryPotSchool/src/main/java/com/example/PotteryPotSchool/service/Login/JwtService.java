package com.example.PotteryPotSchool.service.Login;

import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.security.UserPrincipal;

import java.util.UUID;

public interface JwtService {

    String generateToken(UserEntity user);

    UserPrincipal extractUserPrincipal(String token);

    boolean isTokenValid(String token);
}

