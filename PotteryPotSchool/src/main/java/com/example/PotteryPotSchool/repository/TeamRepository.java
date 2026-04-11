package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Teams.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
    List<TeamEntity> findAllByPost_IdOrderByCreatedAtAsc(UUID postId);
    long countByPost_Id(UUID postId);
}