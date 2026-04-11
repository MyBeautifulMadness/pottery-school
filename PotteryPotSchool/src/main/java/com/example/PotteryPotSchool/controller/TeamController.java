package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Teams.TeamSummary;
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
}