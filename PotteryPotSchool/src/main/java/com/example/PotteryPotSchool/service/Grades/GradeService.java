package com.example.PotteryPotSchool.service.Grades;

import com.example.PotteryPotSchool.dto.Grades.GradeDto;
import com.example.PotteryPotSchool.dto.Grades.GradeUpsertRequest;
import com.example.PotteryPotSchool.dto.Grades.SolutionGradeDto;
import com.example.PotteryPotSchool.dto.Grades.StudentPerformanceDto;
import com.example.PotteryPotSchool.dto.Solutions.MemberGradeDto;

import java.util.UUID;

public interface GradeService {

    SolutionGradeDto upsertGrade(String token, UUID solutionId, GradeUpsertRequest request);

    SolutionGradeDto getGrade(String token, UUID solutionId);

    MemberGradeDto upsertMemberGrade(String token, UUID solutionId, UUID studentId, GradeUpsertRequest request);

    MemberGradeDto getMemberGrade(String token, UUID solutionId, UUID studentId);

    StudentPerformanceDto getStudentPerformance(String token, UUID studentId);

}
