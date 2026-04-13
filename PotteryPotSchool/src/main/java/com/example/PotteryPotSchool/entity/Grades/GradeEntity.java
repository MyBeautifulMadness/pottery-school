package com.example.PotteryPotSchool.entity.Grades;

import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "grades",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"solution_id", "student_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_id", nullable = false)
    private SolutionEntity solution;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private Integer score;

    private String teacherComment;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;
}
