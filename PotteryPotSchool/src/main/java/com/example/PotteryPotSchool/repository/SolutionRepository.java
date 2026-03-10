package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.dto.Solutions.SolutionSummaryDto;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolutionRepository extends JpaRepository<SolutionEntity, UUID> {

    @Query("""
        select new com.example.PotteryPotSchool.dto.Solutions.SolutionSummaryDto(
            s.id,
            s.post.id,
            s.studentId,
            s.status,
            s.submittedAt
        )
        from SolutionEntity s
        where s.post.id = :postId
    """)
    List<SolutionSummaryDto> findByPostId(UUID postId);


    @Query("""
        select new com.example.PotteryPotSchool.dto.Solutions.SolutionSummaryDto(
            s.id,
            s.post.id,
            s.studentId,
            s.status,
            s.submittedAt
        )
        from SolutionEntity s
        where s.post.id = :postId
        and s.status = :status
    """)
    List<SolutionSummaryDto> findByPostIdAndStatus(UUID postId, SolutionStatus status);

    Optional<SolutionEntity> findByPostIdAndStudentId(UUID postId, UUID studentId);
    void deleteAllByPost_Id(UUID postId);
}