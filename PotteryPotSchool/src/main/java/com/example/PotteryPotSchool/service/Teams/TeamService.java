package com.example.PotteryPotSchool.service.Teams;

import com.example.PotteryPotSchool.dto.Teams.*;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    List<TeamSummary> getTeamsByPostId(UUID postId);
    Team createTeam(UUID postId, TeamCreateRequest request);
    Team getTeamById(UUID postId, UUID teamId);
    Team updateTeam(UUID postId, UUID teamId, TeamUpdateRequest request);
    void deleteTeam(UUID postId, UUID teamId);
    Team addStudentToTeam(UUID postId, UUID teamId, UUID studentId);
    void removeStudentFromTeam(UUID postId, UUID teamId, UUID studentId);
    List<Team> manuallyDistributeStudents(UUID postId, ManualTeamDistributionRequest request);
    List<Team> randomlyDistributeStudents(UUID postId);
    List<Team> distributeStudents(UUID postId, TeamDistributionRequest request);
}
