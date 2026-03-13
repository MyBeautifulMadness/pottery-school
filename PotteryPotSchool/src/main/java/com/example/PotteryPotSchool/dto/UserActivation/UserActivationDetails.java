package com.example.PotteryPotSchool.dto.UserActivation;

import com.example.PotteryPotSchool.enums.Users.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class UserActivationDetails {
    private UUID id;
    private String email;
    private Role role;
    private String fullName;
    private String about;
}
