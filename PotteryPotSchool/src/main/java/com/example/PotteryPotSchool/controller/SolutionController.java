package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Solutions.Solution;
import com.example.PotteryPotSchool.dto.Solutions.SolutionUpsertRequest;
import com.example.PotteryPotSchool.service.Solutions.SolutionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}

