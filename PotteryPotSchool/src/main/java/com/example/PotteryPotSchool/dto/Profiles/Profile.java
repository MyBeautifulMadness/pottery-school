package com.example.PotteryPotSchool.dto.Profiles;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Schema(required = true)
    private UUID userId;

    @Schema(required = true)
    private String fullName;

    @Schema(nullable = true)
    private String about;
}
