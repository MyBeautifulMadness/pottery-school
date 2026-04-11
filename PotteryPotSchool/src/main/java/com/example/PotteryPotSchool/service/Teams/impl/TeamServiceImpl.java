package com.example.PotteryPotSchool.service.Teams.impl;

import com.example.PotteryPotSchool.dto.Teams.TeamSummary;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Teams.TeamEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.exception.ForbiddenException;
import com.example.PotteryPotSchool.exception.NotFoundException;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.repository.TeamRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Teams.TeamService;
import com.example.PotteryPotSchool.dto.Users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final PostRepository postRepository;
    private final MeService meService;

    @Override
    public List<TeamSummary> getTeamsByPostId(UUID postId) {
        User currentUser = meService.getMe();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может просматривать список команд задания");
        }

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Пост не найден"));

        if (post.getType() != PostType.TASK || post.getTask() == null) {
            throw new NotFoundException("Задание не найдено");
        }

        if (post.getTask().getMode() != TaskMode.TEAM) {
            throw new NotFoundException("Для данного задания команды недоступны");
        }

        return teamRepository.findAllByPost_IdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    private TeamSummary mapToSummary(TeamEntity team) {
        TeamSummary dto = new TeamSummary();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setMembersCount(team.getMembers() != null ? team.getMembers().size() : 0);
        dto.setCaptainId(team.getCaptain() != null ? team.getCaptain().getId() : null);
        return dto;
    }
}
