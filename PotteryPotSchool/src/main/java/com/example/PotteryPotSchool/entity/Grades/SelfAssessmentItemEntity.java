package com.example.PotteryPotSchool.entity.Grades;

import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Grades.CriterionValueType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "self_assessment_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"solution_id", "criterion_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfAssessmentItemEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_id", nullable = false)
    private SolutionEntity solution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    private CriterionEntity criterion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CriterionValueType valueType;

    @Column(precision = 10, scale = 2)
    private BigDecimal pointsValue;

    private Boolean booleanValue;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal calculatedScore;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
