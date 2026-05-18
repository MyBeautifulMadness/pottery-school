package com.example.PotteryPotSchool.service.Grades;

import com.example.PotteryPotSchool.dto.Grades.*;
import com.example.PotteryPotSchool.dto.Solutions.MemberGradeDto;

import java.util.UUID;

public interface GradeService {

    SolutionGradeDto upsertGrade(UUID solutionId, GradeUpsertRequest request);

    SolutionGradeDto getGrade(UUID solutionId);

    MemberGradeDto upsertMemberGrade(UUID solutionId, UUID studentId, GradeUpsertRequest request);

    MemberGradeDto getMemberGrade(UUID solutionId, UUID studentId);

    StudentPerformanceDto getStudentPerformance(UUID studentId);

    CriterionGradeResult upsertCriterionGrade(UUID solutionId, CriterionGradeUpsertRequest request);

    CriterionGradeResult getCriterionGrade(UUID solutionId);

}
