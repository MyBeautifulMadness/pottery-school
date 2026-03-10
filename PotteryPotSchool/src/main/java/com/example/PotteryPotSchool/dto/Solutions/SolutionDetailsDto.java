package com.example.PotteryPotSchool.dto.Solutions;

import com.example.PotteryPotSchool.dto.Grades.GradeDto;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolutionDetailsDto {

    private UUID id;
    private UUID postId;
    private UUID studentId;
    private SolutionStatus status;

    private String text;
    private String videoUrl;
    private String attachmentUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;

    private GradeDto grade;

}
