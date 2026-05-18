package com.example.PotteryPotSchool.dto.Criteria;

import com.example.PotteryPotSchool.enums.Grades.CriterionImpactType;
import com.example.PotteryPotSchool.enums.Grades.CriterionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CriterionUpdateRequest {
    private String title;
    private String description;
    private CriterionType type;
    private BigDecimal maxScore;
    private CriterionImpactType impactType;
    private Integer displayOrder;
}
