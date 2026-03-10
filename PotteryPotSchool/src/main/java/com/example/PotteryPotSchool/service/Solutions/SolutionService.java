package com.example.PotteryPotSchool.service.Solutions;

import com.example.PotteryPotSchool.dto.Solutions.SolutionDto;
import com.example.PotteryPotSchool.dto.Solutions.SolutionSummaryDto;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.dto.Solutions.Solution;
import com.example.PotteryPotSchool.dto.Solutions.SolutionUpsertRequest;

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

}
