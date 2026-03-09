package com.example.PotteryPotSchool.service.Students;

import com.example.PotteryPotSchool.dto.Students.StudentDetailsDto;
import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;

import java.util.List;
import java.util.UUID;

public interface StudentService {
    List<StudentSummaryDto> getStudents(String token, String q, Integer page, Integer size);
    StudentDetailsDto getStudentById(String token, UUID studentId);
}
