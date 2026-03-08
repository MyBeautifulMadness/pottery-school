package com.example.PotteryPotSchool.service.Login;

import com.example.PotteryPotSchool.entity.Users.UserEntity;

import java.util.UUID;

public interface JwtService {

    String generateToken(UserEntity user);

}

