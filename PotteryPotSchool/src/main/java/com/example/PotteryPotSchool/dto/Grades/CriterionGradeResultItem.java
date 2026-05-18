package com.example.PotteryPotSchool.dto.Grades;

import com.example.PotteryPotSchool.dto.Criteria.CriterionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriterionGradeResultItem {
    private CriterionDto criterion;
    private SelfAssessmentItemDto selfAssessment;
    private TeacherAssessmentItemDto teacherAssessment;
}
