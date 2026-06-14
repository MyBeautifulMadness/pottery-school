package com.example.PotteryPotSchool.dto.Reviews;

import com.example.PotteryPotSchool.enums.Solutions.PeerReviewStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeerReviewDto {

    private UUID id;
    private UUID solutionId;
    private UUID postId;
    private UUID reviewerId;
    private String reviewerName;
    private PeerReviewStatus status;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
}
