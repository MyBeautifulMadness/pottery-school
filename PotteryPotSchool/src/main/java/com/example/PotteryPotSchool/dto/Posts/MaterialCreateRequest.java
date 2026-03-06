package com.example.PotteryPotSchool.dto.Posts;

import com.example.PotteryPotSchool.enums.Posts.MaterialType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class MaterialCreateRequest {

    @Schema(required = true)
    private MaterialType type;

    @Schema(minLength = 1, required = true)
    private String title;

    @Schema(nullable = true)
    private String url;

    @Schema(nullable = true)
    private String text;

}
