package com.example.PotteryPotSchool.dto.Posts;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDetails {
    @Schema(nullable = true)
    private String description;

    @Schema(nullable = true)
    private LocalDateTime deadline;
}
