package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SolutionRepository extends JpaRepository<SolutionEntity, UUID> {
    Optional<SolutionEntity> findByPostIdAndStudent(UUID postId, UUID studentId);
}
