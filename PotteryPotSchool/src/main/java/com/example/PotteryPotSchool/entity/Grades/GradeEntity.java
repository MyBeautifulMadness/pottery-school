package com.example.PotteryPotSchool.entity.Grades;

import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private Integer score;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxFinalScore;

    @Column(precision = 10, scale = 2)
    private BigDecimal regularScore;

    @Column(precision = 10, scale = 2)
    private BigDecimal bonusScore;

    private Integer lateDays;

    @Column(precision = 10, scale = 2)
    private BigDecimal latePenalty;

    private Integer progressMissesCount;

    @Column(precision = 10, scale = 2)
    private BigDecimal progressPenalty;

    @Column(precision = 10, scale = 2)
    private BigDecimal rawScore;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalScore;

    private String teacherComment;

    @Builder.Default
    @OneToMany(mappedBy = "grade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CriterionGradeItemEntity> criterionItems = new ArrayList<>();

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;
}
