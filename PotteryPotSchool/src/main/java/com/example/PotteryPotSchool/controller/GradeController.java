package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Grades.GradeDto;
import com.example.PotteryPotSchool.dto.Grades.GradeUpsertRequest;
import com.example.PotteryPotSchool.dto.Grades.SolutionGradeDto;
import com.example.PotteryPotSchool.dto.Grades.StudentPerformanceDto;
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
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID solutionId,
            @RequestBody GradeUpsertRequest request
    ) {
        String token = extractBearerToken(authHeader);
        return gradeService.upsertGrade(token, solutionId, request);
    }

    @GetMapping("/{solutionId}/grade")
    public SolutionGradeDto getGrade(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID solutionId
    ) {
        String token = extractBearerToken(authHeader);
        return gradeService.getGrade(token, solutionId);
    }

    @PutMapping("/{solutionId}/members/{studentId}/grade")
    public MemberGradeDto upsertMemberGrade(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID solutionId,
            @PathVariable UUID studentId,
            @RequestBody GradeUpsertRequest request
    ) {
        String token = extractBearerToken(authHeader);
        return gradeService.upsertMemberGrade(token, solutionId, studentId, request);
    }

    @GetMapping("/{solutionId}/members/{studentId}/grade")
    public MemberGradeDto getMemberGrade(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID solutionId,
            @PathVariable UUID studentId
    ) {
        String token = extractBearerToken(authHeader);
        return gradeService.getMemberGrade(token, solutionId, studentId);
    }

    @GetMapping("/students/{studentId}/grades")
    public StudentPerformanceDto getStudentPerformance(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID studentId
    ) {
        String token = extractBearerToken(authHeader);
        return gradeService.getStudentPerformance(token, studentId);
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