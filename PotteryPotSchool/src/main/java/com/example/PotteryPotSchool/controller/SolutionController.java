package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Solutions.*;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Solutions.SolutionService;
import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SolutionController {

    private final SolutionService solutionService;

    @PostMapping("/posts/{postId}/solutions")
    public Solution createOrUpdate(@PathVariable UUID postId, @RequestBody SolutionUpsertRequest request) {
        return solutionService.createOrUpdate(postId, request);
    }

    @PostMapping("/solutions/{solutionId}/submit")
    public Solution submit(@PathVariable UUID solutionId) {
        return solutionService.submit(solutionId);
    }

    @GetMapping("/posts/{postId}/solutions")
    public List<SolutionSummaryDto> getSolutions(
            @PathVariable UUID postId,
            @RequestParam(required = false) SolutionStatus status,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return solutionService.getSolutions(postId, status, user);
    }

    @GetMapping("/posts/{postId}/solutions/mine")
    public SolutionDto getMySolution(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return solutionService.getMySolution(postId, user.getId());
    }

    @GetMapping("/solutions/{solutionId}")
    public SolutionDetailsDto getSolution(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID solutionId
    ) {
        return solutionService.getSolution(token, solutionId);
    }
}

