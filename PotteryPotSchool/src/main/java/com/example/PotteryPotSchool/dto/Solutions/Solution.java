package com.example.PotteryPotSchool.dto.Solutions;

import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class Solution {

    @Schema(required = true)
    private UUID id;

    @Schema(required = true)
    private UUID postId;

    @Schema(required = true)
    private UUID studentId;

    @Schema(required = true)
    private SolutionStatus status;

    private String text;

    private String videoUrl;

    private String attachmentUrl;

    @Schema(required = true)
    private LocalDateTime createdAt;

    @Schema(required = true)
    private LocalDateTime updatedAt;

    private LocalDateTime submittedAt;
}
