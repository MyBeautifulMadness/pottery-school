package com.example.PotteryPotSchool.dto.Grades;

import com.example.PotteryPotSchool.enums.Grades.CriterionValueType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CriterionGradeItemRequest {
    private UUID criterionId;
    private CriterionValueType valueType;
    private BigDecimal pointsValue;
    private Boolean booleanValue;
    private BigDecimal percentValue;
    private String teacherComment;
}
