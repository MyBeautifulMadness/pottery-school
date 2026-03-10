package com.example.PotteryPotSchool.dto.Grades;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeUpsertRequest {

    private Integer score;
    private String teacherComment;
}
