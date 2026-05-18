package com.example.PotteryPotSchool.entity.Grades;

import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.enums.Grades.CriterionImpactType;
import com.example.PotteryPotSchool.enums.Grades.CriterionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "criteria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriterionEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CriterionType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CriterionImpactType impactType;

    @Column(nullable = false)
    private Integer displayOrder;
}
