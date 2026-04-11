package com.example.PotteryPotSchool.entity.Posts;

import com.example.PotteryPotSchool.enums.Posts.PrioritySolution;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import com.example.PotteryPotSchool.enums.Posts.TeamDistributionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @OneToOne
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private PostEntity post;
}
