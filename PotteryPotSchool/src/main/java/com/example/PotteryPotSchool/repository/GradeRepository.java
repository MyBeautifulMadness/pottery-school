package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<GradeEntity, UUID> {

    Optional<GradeEntity> findBySolution_Id(UUID solutionId);
    void deleteAllBySolution_Post_Id(UUID postId);

}
