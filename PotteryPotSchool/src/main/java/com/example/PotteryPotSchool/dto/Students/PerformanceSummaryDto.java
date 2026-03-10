package com.example.PotteryPotSchool.dto.Students;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSummaryDto {

    private UUID studentId;

    private Double averageGrade;

    private List<PerformanceItemDto> items;

}