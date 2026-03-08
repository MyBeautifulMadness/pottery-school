package com.example.PotteryPotSchool.security;

import com.example.PotteryPotSchool.enums.Users.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal {

    private UUID id;
    private String email;
    private Role role;
}
