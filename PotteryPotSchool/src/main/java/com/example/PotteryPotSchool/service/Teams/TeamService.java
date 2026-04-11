package com.example.PotteryPotSchool.service.Teams;

import com.example.PotteryPotSchool.dto.Teams.TeamSummary;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    List<TeamSummary> getTeamsByPostId(UUID postId);
}
