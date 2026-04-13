package com.example.PotteryPotSchool.dto.Grades;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentGradeDto {

    private UUID studentId;
    private Integer score;
    private String teacherComment;
    private LocalDateTime gradedAt;
    private UUID teacherId;
}
