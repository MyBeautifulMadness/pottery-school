package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.dto.Students.PageResponse;
import com.example.PotteryPotSchool.dto.Students.PerformanceSummaryDto;
import com.example.PotteryPotSchool.dto.Students.StudentDetailsDto;
import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import com.example.PotteryPotSchool.service.Students.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping()
    public PageResponse<StudentSummaryDto> getStudents(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String q,
            @PageableDefault Pageable pageable
    ) {
        String token = extractToken(authHeader);

        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0 || pageable.getPageSize() > 100) {
            throw new BadRequestException("Invalid pagination parameters");
        }

        return studentService.getStudents(token, q, pageable);
    }

    @GetMapping("/{studentId}")
    public StudentDetailsDto getStudentDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String studentId
    ) {

        String token = extractToken(authHeader);

        UUID uuid;

        try {
            uuid = UUID.fromString(studentId);
        } catch (Exception e) {
            throw new BadRequestException("studentId must be UUID");
        }

        return studentService.getStudentById(token, uuid);
    }

    @GetMapping("/{studentId}/performance")
    public PerformanceSummaryDto getPerformance(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID studentId
    ) {
        return studentService.getStudentPerformance(token, studentId);
    }


    private String extractToken(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}
