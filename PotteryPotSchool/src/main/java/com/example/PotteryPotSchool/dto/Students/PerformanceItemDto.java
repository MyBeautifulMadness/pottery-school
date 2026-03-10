package com.example.PotteryPotSchool.dto.Students;

import com.example.PotteryPotSchool.dto.Grades.GradeDto;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceItemDto {

    private UUID postId;
    private String postTitle;

    private UUID solutionId;

    private GradeDto grade;

}
