package com.example.PotteryPotSchool.dto.Solutions;

import lombok.Data;

@Data
public class SolutionUpdateRequest {
    private String text;
    private String videoUrl;
    private String attachmentUrl;
}