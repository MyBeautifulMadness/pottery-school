package com.example.PotteryPotSchool.dto.Solutions;


import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberGradeDto {

    private UUID solutionId;
    private UUID studentId;
    private Integer score;
    private String teacherComment;
    private LocalDateTime gradedAt;
    private UUID teacherId;
}
