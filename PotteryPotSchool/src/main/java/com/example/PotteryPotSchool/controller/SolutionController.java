package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Solutions.SolutionSummaryDto;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Solutions.SolutionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class SolutionController {

    private final SolutionService solutionService;

    @GetMapping("/{postId}/solutions")
    public List<SolutionSummaryDto> getSolutions(
            @PathVariable UUID postId,
            @RequestParam(required = false) SolutionStatus status,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return solutionService.getSolutions(postId, status, user);
    }
}
