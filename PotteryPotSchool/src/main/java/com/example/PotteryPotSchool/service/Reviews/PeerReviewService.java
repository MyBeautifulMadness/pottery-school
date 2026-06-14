package com.example.PotteryPotSchool.service.Reviews;

import com.example.PotteryPotSchool.dto.Reviews.PeerReviewAssignmentDto;
import com.example.PotteryPotSchool.dto.Reviews.PeerReviewDto;
import com.example.PotteryPotSchool.dto.Reviews.PeerReviewUpsertRequest;

import java.util.List;
import java.util.UUID;

public interface PeerReviewService {

    List<PeerReviewDto> assign(UUID postId);

    List<PeerReviewAssignmentDto> getMyAssignments(UUID postId);

    PeerReviewDto upsertReview(UUID solutionId, PeerReviewUpsertRequest request);

    PeerReviewDto getMyReview(UUID solutionId);

    List<PeerReviewDto> getReviewsForSolution(UUID solutionId);
}
