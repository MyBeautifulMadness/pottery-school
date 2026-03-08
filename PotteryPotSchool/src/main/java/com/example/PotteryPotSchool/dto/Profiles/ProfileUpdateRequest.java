package com.example.PotteryPotSchool.dto.Profiles;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "fullName must not be blank")
    @Schema(required = true)
    private String fullName;

    @Schema(nullable = true)
    private String about;
}
