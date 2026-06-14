package com.example.PotteryPotSchool.entity.Posts;

import com.example.PotteryPotSchool.entity.Grades.CriterionEntity;
import com.example.PotteryPotSchool.enums.Posts.PrioritySolution;
import com.example.PotteryPotSchool.enums.Posts.ReviewType;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import com.example.PotteryPotSchool.enums.Posts.TeamDistributionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskMode mode;

    @Enumerated(EnumType.STRING)
    private TeamDistributionType teamDistributionType;

    private LocalDateTime formationDeadline;
    private Integer minTeamsCount;
    private Integer maxTeamsCount;
    private Integer minMembersPerTeam;
    private Integer maxMembersPerTeam;

    @Enumerated(EnumType.STRING)
    private PrioritySolution prioritySolution;

    @Column(columnDefinition = "uuid")
    private UUID selectedSolutionId;

    @Builder.Default
    private Boolean gradingEnabled = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxFinalScore;

    @Builder.Default
    private Boolean selfAssessmentRequired = false;

    @Builder.Default
    private Boolean latePenaltyEnabled = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal latePenaltyPerDay;

    @Builder.Default
    private Boolean progressPenaltyEnabled = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal progressPenaltyPerMiss;


    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ReviewType reviewType = ReviewType.NORMAL;

    private Integer reviewsPerStudent;

    private LocalDateTime reviewDeadline;

    @Builder.Default
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<CriterionEntity> criteria = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private PostEntity post;
}
