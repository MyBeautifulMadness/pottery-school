package com.example.PotteryPotSchool.dto.Solutions;

import com.example.PotteryPotSchool.enums.Solutions.SolutionOwnerType;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solution {

    private UUID id;
    private UUID postId;
    private SolutionOwnerType ownerType;

    private String studentName;
    private UUID studentId;
    private UUID teamId;

    private SolutionStatus status;

    private String text;
    private String videoUrl;
    private String attachmentUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;

    private UUID authorStudentId;

    private Integer votesCount;
}