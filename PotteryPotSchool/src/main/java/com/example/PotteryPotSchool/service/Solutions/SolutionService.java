package com.example.PotteryPotSchool.service.Solutions;

import com.example.PotteryPotSchool.dto.Solutions.*;
import com.example.PotteryPotSchool.enums.Solutions.*;
import com.example.PotteryPotSchool.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface SolutionService {

    Solution create(UUID postId, SolutionCreateRequest request);

    Solution update(UUID solutionId, SolutionUpdateRequest request);

    Solution submit(UUID solutionId);

    List<SolutionSummaryDto> getSolutions(
            UUID postId,
            SolutionStatus status,
            SolutionOwnerType ownerType,
            UUID teamId,
            UUID studentId,
            UUID authorStudentId,
            Boolean selectedOnly,
            UserPrincipal user
    );

    Solution getMySolution(UUID postId, UUID studentId);

    SolutionDetailsDto getSolution(UserPrincipal user, UUID solutionId);

    List<SolutionSummaryDto> getTeamSolutions(UUID postId, UUID studentId);

    Solution getSelected(UUID postId);

    Solution vote(UUID solutionId);

    Solution unvote(UUID solutionId);

    Solution unsubmit(UUID solutionId);

}