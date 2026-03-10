package com.example.PotteryPotSchool.service.Grades;

import com.example.PotteryPotSchool.dto.Grades.GradeDto;
import com.example.PotteryPotSchool.dto.Grades.GradeUpsertRequest;

import java.util.UUID;

public interface GradeService {

    GradeDto upsertGrade(String token, UUID solutionId, GradeUpsertRequest request);

    GradeDto getGrade(String token, UUID solutionId);

}
