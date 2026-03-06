package com.example.PotteryPotSchool.dto.Posts;

import com.example.PotteryPotSchool.enums.Posts.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostCreateRequest {


    @Schema(required = true)
    @Enumerated(EnumType.STRING)
    private PostType type;

    @Schema(minLength = 1, required = true)
    private String title;

    @Schema(nullable = true)
    private String description;


    private MaterialCreateRequest material;
    private TaskCreateRequest task;
}
