package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.dto.Students.StudentDetailsDto;
import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import com.example.PotteryPotSchool.service.Students.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public List<StudentSummaryDto> getStudents(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {

        String token = extractToken(authHeader);

        return studentService.getStudents(token, q, page, size);
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


    private String extractToken(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}
