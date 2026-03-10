package com.example.PotteryPotSchool.service.Solutions;

import com.example.PotteryPotSchool.dto.Solutions.*;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface SolutionService {
    Solution createOrUpdate(UUID postId, SolutionUpsertRequest request);
    Solution submit(UUID solutionId);

    List<SolutionSummaryDto> getSolutions(
            UUID postId,
            SolutionStatus status,
            UserPrincipal user
    );

    SolutionDto getMySolution(UUID postId, UUID studentId);

    SolutionDetailsDto getSolution(String token, UUID solutionId);

}
