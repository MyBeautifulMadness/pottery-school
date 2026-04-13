package com.example.PotteryPotSchool.dto.Grades;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolutionGradeDto {

    private UUID solutionId;
    private Integer teamGrade;
    private List<StudentGradeDto> grades;
}
