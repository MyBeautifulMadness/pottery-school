package com.example.PotteryPotSchool.dto.UserActivation;

import com.example.PotteryPotSchool.enums.Users.Role;
import lombok.Data;

@Data
public class UserActivationCreateRequest {
    private String email;
    private String password;
    private Role role;
    private String fullName;
    private String about;
}
