package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<GradeEntity, UUID> {

    Optional<GradeEntity> findBySolution_Id(UUID solutionId);
    void deleteAllBySolution_Post_Id(UUID postId);
    Optional<GradeEntity> findBySolution_IdAndStudentId(UUID solutionId, UUID studentId);
    List<GradeEntity> findAllBySolution_Id(UUID solutionId);
    List<GradeEntity> findAllByStudentId(UUID studentId);

}
