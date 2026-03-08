package com.example.PotteryPotSchool.dto.Users;

import com.example.PotteryPotSchool.enums.Users.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
public class User {

    @Schema(required = true)
    private UUID id;

    @Schema(required = true)
    private String email;

    @Schema(required = true)
    private Role role;
}