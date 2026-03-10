package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Grades.GradeDto;
import com.example.PotteryPotSchool.dto.Grades.GradeUpsertRequest;
import com.example.PotteryPotSchool.service.Grades.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solutions")
public class GradeController {

    private final GradeService gradeService;

    @PutMapping("/{solutionId}/grade")
    public GradeDto upsertGrade(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID solutionId,
            @RequestBody GradeUpsertRequest request
    ) {
        String token = extractToken(authHeader);
        return gradeService.upsertGrade(token, solutionId, request);
    }

    @GetMapping("/{solutionId}/grade")
    public GradeDto getGrade(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID solutionId
    ) {
        return gradeService.getGrade(token, solutionId);
    }

    private String extractToken(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}
