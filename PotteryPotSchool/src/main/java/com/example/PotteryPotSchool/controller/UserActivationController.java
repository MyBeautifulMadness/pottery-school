package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.UserActivation.UserActivationCreateRequest;
import com.example.PotteryPotSchool.dto.UserActivation.UserActivationDetails;
import com.example.PotteryPotSchool.service.UserActivation.UserActivation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activateUser")
@RequiredArgsConstructor
public class UserActivationController {

    private final UserActivation userManagementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserActivationDetails createUser(@RequestBody UserActivationCreateRequest request) {
        return userManagementService.activation(request);
    }

}
