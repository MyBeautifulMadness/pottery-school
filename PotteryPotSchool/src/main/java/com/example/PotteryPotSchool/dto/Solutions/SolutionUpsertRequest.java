package com.example.PotteryPotSchool.dto.Solutions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SolutionUpsertRequest {

    private String text;

    private String videoUrl;

    private String attachmentUrl;

    @Schema(defaultValue = "false")
    private Boolean submit;
}
