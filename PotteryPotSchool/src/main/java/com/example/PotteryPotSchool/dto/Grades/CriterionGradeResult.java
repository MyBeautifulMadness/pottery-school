package com.example.PotteryPotSchool.dto.Grades;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriterionGradeResult {
    private UUID solutionId;
    private UUID postId;
    private BigDecimal maxFinalScore;
    private BigDecimal regularScore;
    private BigDecimal bonusScore;
    private Integer lateDays;
    private BigDecimal latePenalty;
    private Integer progressMissesCount;
    private BigDecimal progressPenalty;
    private BigDecimal rawScore;
    private BigDecimal finalScore;
    private LocalDateTime gradedAt;
    private UUID teacherId;
    private List<CriterionGradeResultItem> items;
}
