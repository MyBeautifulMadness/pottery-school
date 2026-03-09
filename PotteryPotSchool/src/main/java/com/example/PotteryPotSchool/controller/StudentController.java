package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import com.example.PotteryPotSchool.service.Students.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
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

    private String extractToken(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}
