package com.example.PotteryPotSchool.dto.Criteria;

import com.example.PotteryPotSchool.enums.Grades.CriterionImpactType;
import com.example.PotteryPotSchool.enums.Grades.CriterionType;
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
public class CriterionDto {
    private UUID id;
    private UUID postId;
    private String title;
    private String description;
    private CriterionType type;
    private BigDecimal maxScore;
    private CriterionImpactType impactType;
    private Integer displayOrder;
}
