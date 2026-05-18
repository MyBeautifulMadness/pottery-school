package com.example.PotteryPotSchool.dto.Posts;

import com.example.PotteryPotSchool.dto.Criteria.CriterionCreateRequest;
import com.example.PotteryPotSchool.enums.Posts.PrioritySolution;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import com.example.PotteryPotSchool.enums.Posts.TeamDistributionType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskUpdateRequest {
    private String description;
    private LocalDateTime deadline;
    private TaskMode mode;
    private TeamDistributionType teamDistributionType;
    private TeamRules teamRules;
    private PrioritySolution prioritySolution;
    private TaskGradingSettings gradingSettings;
    private List<CriterionCreateRequest> criteria;
}
