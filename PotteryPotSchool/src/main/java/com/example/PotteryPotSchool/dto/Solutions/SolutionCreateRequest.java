package com.example.PotteryPotSchool.dto.Solutions;

import com.example.PotteryPotSchool.dto.Grades.SelfAssessmentItemRequest;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SolutionCreateRequest {
    private String text;
    private String videoUrl;
    private String attachmentUrl;
    private Boolean submit;
    private UUID teamId;
    private List<SelfAssessmentItemRequest> selfAssessment;
}