package com.example.PotteryPotSchool.dto.Login;

import com.example.PotteryPotSchool.dto.Users.User;
import lombok.Data;

@Data
public class LoginResponse {

    private String accessToken;
    private User user;
}