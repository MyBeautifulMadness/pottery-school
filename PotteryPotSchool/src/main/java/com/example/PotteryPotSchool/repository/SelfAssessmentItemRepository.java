package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Grades.SelfAssessmentItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SelfAssessmentItemRepository extends JpaRepository<SelfAssessmentItemEntity, UUID> {
    List<SelfAssessmentItemEntity> findAllBySolution_Id(UUID solutionId);
    void deleteAllBySolution_Id(UUID solutionId);
    void deleteAllBySolution_Post_Id(UUID postId);
    void deleteBySolution_Id(UUID solutionId);
}
