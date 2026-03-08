package com.example.PotteryPotSchool.service.Login;

import com.example.PotteryPotSchool.dto.Login.LoginRequest;
import com.example.PotteryPotSchool.dto.Login.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

}
