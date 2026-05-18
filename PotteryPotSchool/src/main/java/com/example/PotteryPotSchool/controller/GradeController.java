package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Grades.*;
import com.example.PotteryPotSchool.dto.Solutions.MemberGradeDto;
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
    public SolutionGradeDto upsertGrade(
            @PathVariable UUID solutionId,
            @RequestBody GradeUpsertRequest request
    ) {
        return gradeService.upsertGrade(solutionId, request);
    }

    @GetMapping("/{solutionId}/grade")
    public SolutionGradeDto getGrade(
            @PathVariable UUID solutionId
    ) {
        return gradeService.getGrade(solutionId);
    }

    @GetMapping("/{solutionId}/criterion-grade")
    public CriterionGradeResult getCriterionGrade(
            @PathVariable UUID solutionId
    ) {
        return gradeService.getCriterionGrade(solutionId);
    }

    @PutMapping("/{solutionId}/criterion-grade")
    public CriterionGradeResult upsertCriterionGrade(
            @PathVariable UUID solutionId,
            @RequestBody CriterionGradeUpsertRequest request
    ) {
        return gradeService.upsertCriterionGrade(solutionId, request);
    }

    @PutMapping("/{solutionId}/members/{studentId}/grade")
    public MemberGradeDto upsertMemberGrade(
            @PathVariable UUID solutionId,
            @PathVariable UUID studentId,
            @RequestBody GradeUpsertRequest request
    ) {
        return gradeService.upsertMemberGrade(solutionId, studentId, request);
    }

    @GetMapping("/{solutionId}/members/{studentId}/grade")
    public MemberGradeDto getMemberGrade(
            @PathVariable UUID solutionId,
            @PathVariable UUID studentId
    ) {
        return gradeService.getMemberGrade(solutionId, studentId);
    }

    @GetMapping("/students/{studentId}/grades")
    public StudentPerformanceDto getStudentPerformance(
            @PathVariable UUID studentId
    ) {
        return gradeService.getStudentPerformance(studentId);
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedException("Authorization header is missing");
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid authorization header");
        }

        return authHeader.substring(7);
    }
}