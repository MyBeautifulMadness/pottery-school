package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Profiles.Profile;
import com.example.PotteryPotSchool.dto.Profiles.ProfileUpdateRequest;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.service.Me.MeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/profile")
    public Profile updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return meService.updateMyProfile(request);
    }
}
