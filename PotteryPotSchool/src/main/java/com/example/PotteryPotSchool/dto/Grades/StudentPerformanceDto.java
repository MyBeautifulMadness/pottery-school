package com.example.PotteryPotSchool.dto.Grades;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPerformanceDto {

    private UUID studentId;
    private List<StudentPerformanceItemDto> grades;
}
