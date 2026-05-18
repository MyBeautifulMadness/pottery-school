package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Grades.CriterionGradeItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CriterionGradeItemRepository extends JpaRepository<CriterionGradeItemEntity, UUID> {
    List<CriterionGradeItemEntity> findAllByGrade_Id(UUID gradeId);
    void deleteAllByGrade_Solution_Post_Id(UUID postId);
}
