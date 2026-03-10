package com.example.PotteryPotSchool.service.Solutions;

import com.example.PotteryPotSchool.dto.Solutions.Solution;
import com.example.PotteryPotSchool.dto.Solutions.SolutionUpsertRequest;

import java.util.UUID;

public interface SolutionService {
    Solution createOrUpdate(UUID postId, SolutionUpsertRequest request);
    Solution submit(UUID solutionId);
}
