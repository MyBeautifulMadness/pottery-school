package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Grades.CriterionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CriterionRepository extends JpaRepository<CriterionEntity, UUID> {
    List<CriterionEntity> findAllByTask_Post_IdOrderByDisplayOrderAsc(UUID postId);
    void deleteAllByTask_Post_Id(UUID postId);
}
