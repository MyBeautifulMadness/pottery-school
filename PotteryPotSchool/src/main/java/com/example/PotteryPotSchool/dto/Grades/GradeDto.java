package com.example.PotteryPotSchool.dto.Grades;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDto {

    private UUID solutionId;
    private Integer score;
    private String teacherComment;
    private LocalDateTime gradedAt;
    private UUID teacherId;
}
