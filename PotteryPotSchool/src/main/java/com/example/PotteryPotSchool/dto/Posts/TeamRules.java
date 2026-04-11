package com.example.PotteryPotSchool.dto.Posts;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamRules {
    private LocalDateTime formationDeadline;
    private Integer minTeamsCount;
    private Integer maxTeamsCount;
    private Integer minMembersPerTeam;
    private Integer maxMembersPerTeam;
}
