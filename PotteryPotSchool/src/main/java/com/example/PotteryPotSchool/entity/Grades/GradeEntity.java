package com.example.PotteryPotSchool.entity.Grades;

import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "solution_id", nullable = false, unique = true)
    private SolutionEntity solution;

    @Column(nullable = false)
    private Integer score;

    private String teacherComment;

    @Column(nullable = false)
    private UUID teacherId;

    @Column(nullable = false)
    private LocalDateTime gradedAt;
}
