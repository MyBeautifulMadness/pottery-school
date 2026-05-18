package com.example.PotteryPotSchool.dto.Grades;

import lombok.Data;

import java.util.List;

@Data
public class CriterionGradeUpsertRequest {
    private List<CriterionGradeItemRequest> items;
    private Integer progressMissesCount;
}
