package com.example.PotteryPotSchool.dto.Reviews;

import com.example.PotteryPotSchool.dto.Solutions.Solution;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeerReviewAssignmentDto {

    private PeerReviewDto review;
    private Solution solution;
    private LocalDateTime reviewDeadline;
}
