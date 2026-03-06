package com.example.PotteryPotSchool.dto.Posts;

import com.example.PotteryPotSchool.enums.Posts.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PostDetails {

    @Schema(required = true)
    private UUID id;

    @Schema(required = true)
    @Enumerated(EnumType.STRING)
    private PostType type;

    @Schema(required = true)
    private String title;

    @Schema(nullable = true)
    private String description;

    @Schema(required = true)
    private LocalDateTime createdAt;

    @Schema(required = true)
    private LocalDateTime updatedAt;

    private Material material;
    private TaskDetails task;
}
