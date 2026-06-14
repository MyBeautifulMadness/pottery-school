package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Reviews.PeerReviewEntity;
import com.example.PotteryPotSchool.enums.Solutions.PeerReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeerReviewRepository extends JpaRepository<PeerReviewEntity, UUID> {

    Optional<PeerReviewEntity> findBySolution_IdAndReviewerId(UUID solutionId, UUID reviewerId);

    List<PeerReviewEntity> findAllByPostIdAndReviewerId(UUID postId, UUID reviewerId);

    List<PeerReviewEntity> findAllBySolution_Id(UUID solutionId);

    List<PeerReviewEntity> findAllBySolution_IdAndStatus(UUID solutionId, PeerReviewStatus status);

    void deleteAllByPostId(UUID postId);

    void deleteAllBySolution_Post_Id(UUID postId);
}
