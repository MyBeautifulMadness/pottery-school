package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Profiles.Profile;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.service.Me.MeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/me")
public class MeController {
    private final MeService meService;

    @GetMapping
    public User getMe() {
        return meService.getMe();
    }

    @GetMapping("/profile")
    public Profile getMyProfile() {
        return meService.getMyProfile();
    }
}
