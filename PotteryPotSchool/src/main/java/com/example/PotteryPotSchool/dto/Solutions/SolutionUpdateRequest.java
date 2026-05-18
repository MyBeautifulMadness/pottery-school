package com.example.PotteryPotSchool.dto.Solutions;

import com.example.PotteryPotSchool.dto.Grades.SelfAssessmentItemRequest;
import lombok.Data;

import java.util.List;

@Data
public class SolutionUpdateRequest {
    private String text;
    private String videoUrl;
    private String attachmentUrl;
    private List<SelfAssessmentItemRequest> selfAssessment;
}