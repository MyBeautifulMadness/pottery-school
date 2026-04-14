package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Solutions.*;
import com.example.PotteryPotSchool.enums.Solutions.*;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Solutions.SolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SolutionController {

    private final SolutionService service;

    @PostMapping("/posts/{postId}/solutions")
    public Solution create(@PathVariable UUID postId, @RequestBody SolutionCreateRequest request) {
        return service.create(postId, request);
    }

    @GetMapping("/posts/{postId}/solutions")
    public List<SolutionSummaryDto> getSolutions(
            @PathVariable UUID postId,
            @RequestParam(required = false) SolutionStatus status,
            @RequestParam(required = false) SolutionOwnerType ownerType,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID authorStudentId,
            @RequestParam(required = false) Boolean selectedOnly,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return service.getSolutions(postId, status, ownerType, teamId, studentId, authorStudentId, selectedOnly, user);
    }

    @GetMapping("/posts/{postId}/solutions/mine")
    public Solution mine(@PathVariable UUID postId, @AuthenticationPrincipal UserPrincipal user) {
        return service.getMySolution(postId, user.getId());
    }

    @GetMapping("/posts/{postId}/solutions/selected")
    public Solution selected(@PathVariable UUID postId) {
        return service.getSelected(postId);
    }

    @GetMapping("/solutions/{solutionId}")
    public SolutionDetailsDto get(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID solutionId
    ) {
        return service.getSolution(user, solutionId);
    }

    @PatchMapping("/solutions/{solutionId}")
    public Solution update(@PathVariable UUID solutionId, @RequestBody SolutionUpdateRequest request) {
        return service.update(solutionId, request);
    }

    @PostMapping("/solutions/{solutionId}/submit")
    public Solution submit(@PathVariable UUID solutionId) {
        return service.submit(solutionId);
    }

    @PostMapping("/solutions/{solutionId}/vote")
    public Solution vote(@PathVariable UUID solutionId) {
        return service.vote(solutionId);
    }

    @DeleteMapping("/solutions/{solutionId}/vote")
    public Solution unvote(@PathVariable UUID solutionId) {
        return service.unvote(solutionId);
    }

    @GetMapping("/posts/{postId}/solutions/team")
    public List<SolutionSummaryDto> team(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return service.getTeamSolutions(postId, user.getId());
    }

    @DeleteMapping("/solutions/{solutionId}/unsubmit")
    public Solution unsubmit(@PathVariable UUID solutionId) {
        return service.unsubmit(solutionId);
    }
}