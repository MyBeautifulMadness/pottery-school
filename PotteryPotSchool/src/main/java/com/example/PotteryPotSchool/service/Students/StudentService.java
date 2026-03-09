package com.example.PotteryPotSchool.service.Students;

import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;

import java.util.List;

public interface StudentService {
    List<StudentSummaryDto> getStudents(String token, String q, Integer page, Integer size);
}
