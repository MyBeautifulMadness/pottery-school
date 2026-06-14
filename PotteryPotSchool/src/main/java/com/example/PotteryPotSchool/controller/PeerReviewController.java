package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Reviews.PeerReviewAssignmentDto;
import com.example.PotteryPotSchool.dto.Reviews.PeerReviewDto;
import com.example.PotteryPotSchool.dto.Reviews.PeerReviewUpsertRequest;
import com.example.PotteryPotSchool.service.Reviews.PeerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PeerReviewController {

    private final PeerReviewService peerReviewService;

    @PostMapping("/posts/{postId}/peer-reviews/assign")
    public List<PeerReviewDto> assign(@PathVariable UUID postId) {
        return peerReviewService.assign(postId);
    }

    @GetMapping("/posts/{postId}/peer-reviews/mine")
    public List<PeerReviewAssignmentDto> myAssignments(@PathVariable UUID postId) {
        return peerReviewService.getMyAssignments(postId);
    }

    @PutMapping("/solutions/{solutionId}/peer-review")
    public PeerReviewDto upsertReview(
            @PathVariable UUID solutionId,
            @RequestBody PeerReviewUpsertRequest request
    ) {
        return peerReviewService.upsertReview(solutionId, request);
    }

    @GetMapping("/solutions/{solutionId}/peer-review")
    public PeerReviewDto myReview(@PathVariable UUID solutionId) {
        return peerReviewService.getMyReview(solutionId);
    }

    @GetMapping("/solutions/{solutionId}/peer-reviews")
    public List<PeerReviewDto> reviewsForSolution(@PathVariable UUID solutionId) {
        return peerReviewService.getReviewsForSolution(solutionId);
    }
}
