package com.example.PotteryPotSchool.service.Students;

import com.example.PotteryPotSchool.dto.Students.PageResponse;
import com.example.PotteryPotSchool.dto.Students.PerformanceSummaryDto;
import com.example.PotteryPotSchool.dto.Students.StudentDetailsDto;
import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentService {
    PageResponse<StudentSummaryDto> getStudents(
            String token,
            String query,
            Pageable pageable
    );
    StudentDetailsDto getStudentById(String token, UUID studentId);
}
