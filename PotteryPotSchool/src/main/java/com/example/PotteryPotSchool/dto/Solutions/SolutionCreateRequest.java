package com.example.PotteryPotSchool.dto.Solutions;

import lombok.Data;

import java.util.UUID;

@Data
public class SolutionCreateRequest {
    private String text;
    private String videoUrl;
    private String attachmentUrl;
    private Boolean submit;
    private UUID teamId;
}