package com.example.PotteryPotSchool.dto.Grades;

import com.example.PotteryPotSchool.enums.Grades.GradeSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    private GradeSource source;
    private BigDecimal peerAverageScore;
    private Integer peerReviewsCount;
}
