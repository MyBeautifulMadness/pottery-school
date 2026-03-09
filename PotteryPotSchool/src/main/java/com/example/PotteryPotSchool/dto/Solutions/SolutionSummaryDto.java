package com.example.PotteryPotSchool.dto.Solutions;

import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolutionSummaryDto {

    private UUID id;
    private UUID postId;
    private UUID studentId;
    private SolutionStatus status;
    private LocalDateTime submittedAt;

}