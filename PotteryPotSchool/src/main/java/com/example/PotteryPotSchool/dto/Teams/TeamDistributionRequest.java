package com.example.PotteryPotSchool.dto.Teams;

import com.example.PotteryPotSchool.enums.Posts.TeamDistributionType;
import lombok.Data;

@Data
public class TeamDistributionRequest {
    private TeamDistributionType strategy;
    private Integer teamsCount;
    private Integer maxMembersPerTeam;
}
