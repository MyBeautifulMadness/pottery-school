package com.example.PotteryPotSchool.service.Solutions;

import com.example.PotteryPotSchool.dto.Solutions.SolutionSummaryDto;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface SolutionService {

    List<SolutionSummaryDto> getSolutions(
            UUID postId,
            SolutionStatus status,
            UserPrincipal user
    );

}
