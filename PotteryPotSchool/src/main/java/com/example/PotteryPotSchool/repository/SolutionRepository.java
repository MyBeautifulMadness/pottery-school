package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Solutions.SolutionOwnerType;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolutionRepository extends JpaRepository<SolutionEntity, UUID> {


    List<SolutionEntity> findByPostId(UUID postId);

    List<SolutionEntity> findByPostIdAndStatus(UUID postId, SolutionStatus status);

    List<SolutionEntity> findByPostIdAndOwnerType(UUID postId, SolutionOwnerType ownerType);

    List<SolutionEntity> findByPostIdAndTeamId(UUID postId, UUID teamId);


    Optional<SolutionEntity> findByPostIdAndStudentId(UUID postId, UUID studentId);

    boolean existsByPostIdAndStudentId(UUID postId, UUID studentId);

    void deleteAllByPost_Id(UUID postId);


    @Query("""
        select s
        from SolutionEntity s
        join fetch s.post
        where s.post.id = :postId
    """)
    List<SolutionEntity> findByPostIdWithPost(@Param("postId") UUID postId);

    @Query("""
        select s
        from SolutionEntity s
        join fetch s.post
        where s.post.id = :postId
        and s.status = :status
    """)
    List<SolutionEntity> findByPostIdAndStatusWithPost(
            @Param("postId") UUID postId,
            @Param("status") SolutionStatus status
    );
}