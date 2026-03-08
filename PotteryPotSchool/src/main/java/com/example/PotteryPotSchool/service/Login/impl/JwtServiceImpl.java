package com.example.PotteryPotSchool.service.Login.impl;

import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.service.Login.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey key = Keys.hmacShaKeyFor(
            "verySecretKeyverySecretKeyverySecretKey".getBytes()
    );

    @Override
    public String generateToken(UserEntity user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }
}
