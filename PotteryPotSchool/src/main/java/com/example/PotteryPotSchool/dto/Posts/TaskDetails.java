package com.example.PotteryPotSchool.dto.Posts;

import com.example.PotteryPotSchool.enums.Posts.PrioritySolution;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import com.example.PotteryPotSchool.enums.Posts.TeamDistributionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskDetails {
    @Schema(nullable = true)
    private String description;

    @Schema(nullable = true)
    private LocalDateTime deadline;

    private TaskMode mode;
    private TeamDistributionType teamDistributionType;
    private TeamRules teamRules;
    private PrioritySolution prioritySolution;

    @Schema(nullable = true)
    private UUID selectedSolutionId;
}
