package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Teams.*;
import com.example.PotteryPotSchool.service.Teams.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public List<TeamSummary> getTeamsByPostId(@PathVariable UUID postId) {
        return teamService.getTeamsByPostId(postId);
    }

    @PostMapping
    public Team createTeam(@PathVariable UUID postId,
                           @RequestBody TeamCreateRequest request) {
        return teamService.createTeam(postId, request);
    }

    @GetMapping("/{teamId}")
    public Team getTeamById(@PathVariable UUID postId,
                            @PathVariable UUID teamId) {
        return teamService.getTeamById(postId, teamId);
    }

    @PatchMapping("/{teamId}")
    public Team updateTeam(@PathVariable UUID postId,
                           @PathVariable UUID teamId,
                           @RequestBody TeamUpdateRequest request) {
        return teamService.updateTeam(postId, teamId, request);
    }

    @DeleteMapping("/{teamId}")
    public void deleteTeam(@PathVariable UUID postId,
                           @PathVariable UUID teamId) {
        teamService.deleteTeam(postId, teamId);
    }

    @PostMapping("/{teamId}/members/{studentId}")
    public Team addStudentToTeam(@PathVariable UUID postId,
                                 @PathVariable UUID teamId,
                                 @PathVariable UUID studentId) {
        return teamService.addStudentToTeam(postId, teamId, studentId);
    }

    @DeleteMapping("/{teamId}/members/{studentId}")
    public void removeStudentFromTeam(@PathVariable UUID postId,
                                      @PathVariable UUID teamId,
                                      @PathVariable UUID studentId) {
        teamService.removeStudentFromTeam(postId, teamId, studentId);
    }

    @PostMapping("/distribute/manual")
    public List<Team> manuallyDistributeStudents(@PathVariable UUID postId,
                                                 @RequestBody ManualTeamDistributionRequest request) {
        return teamService.manuallyDistributeStudents(postId, request);
    }
}