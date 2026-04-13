package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Solutions.SolutionVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SolutionVoteRepository extends JpaRepository<SolutionVote, UUID> {

    boolean existsByPostIdAndStudentId(UUID postId, UUID studentId);

    void deleteByPostIdAndStudentId(UUID postId, UUID studentId);

    List<SolutionVote> findAllByPostId(UUID postId);

    long countBySolutionId(UUID solutionId);
}