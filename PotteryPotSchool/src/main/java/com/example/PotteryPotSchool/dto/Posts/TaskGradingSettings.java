package com.example.PotteryPotSchool.dto.Posts;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaskGradingSettings {
    private Boolean enabled;

    @DecimalMin(value = "0.01", message = "maxFinalScore должен быть больше 0")
    @DecimalMax(value = "100.00", message = "maxFinalScore не должен быть больше 100")
    private BigDecimal maxFinalScore;
    private Boolean selfAssessmentRequired;
    private Boolean latePenaltyEnabled;
    private BigDecimal latePenaltyPerDay;
    private Boolean progressPenaltyEnabled;
    private BigDecimal progressPenaltyPerMiss;
}
