package com.example.PotteryPotSchool.dto.Grades;

import com.example.PotteryPotSchool.enums.Grades.CriterionValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfAssessmentItemDto {
    private UUID criterionId;
    private CriterionValueType valueType;
    private BigDecimal pointsValue;
    private Boolean booleanValue;
    private BigDecimal percentValue;
    private BigDecimal calculatedScore;
    private String comment;
}
