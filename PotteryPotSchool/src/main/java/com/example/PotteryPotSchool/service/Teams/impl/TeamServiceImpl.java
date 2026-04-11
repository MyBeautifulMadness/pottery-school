package com.example.PotteryPotSchool.service.Teams.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import com.example.PotteryPotSchool.dto.Teams.Team;
import com.example.PotteryPotSchool.dto.Teams.TeamCreateRequest;
import com.example.PotteryPotSchool.dto.Teams.TeamSummary;
import com.example.PotteryPotSchool.dto.Teams.TeamUpdateRequest;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Teams.TeamEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.exception.ForbiddenException;
import com.example.PotteryPotSchool.exception.NotFoundException;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.repository.TeamRepository;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Teams.TeamService;
import com.example.PotteryPotSchool.dto.Users.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MeService meService;

    @Override
    public List<TeamSummary> getTeamsByPostId(UUID postId) {
        User currentUser = meService.getMe();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может просматривать список команд задания");
        }

        PostEntity post = getTeamTaskPost(postId);

        return teamRepository.findAllByPost_IdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    @Transactional
    public Team createTeam(UUID postId, TeamCreateRequest request) {
        User currentUser = meService.getMe();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может создавать команды");
        }

        PostEntity post = getTeamTaskPost(postId);
        validateCreateRequest(request);

        LinkedHashSet<UserEntity> members = new LinkedHashSet<>();

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (UUID memberId : request.getMemberIds()) {
                UserEntity member = userRepository.findById(memberId)
                        .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + memberId));

                if (member.getRole() != Role.STUDENT) {
                    throw new BadRequestException("Участником команды может быть только студент");
                }

                ensureStudentNotInAnotherTeam(postId, memberId);
                members.add(member);
            }
        }

        UserEntity captain = null;
        if (request.getCaptainId() != null) {
            captain = userRepository.findById(request.getCaptainId())
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + request.getCaptainId()));

            if (captain.getRole() != Role.STUDENT) {
                throw new BadRequestException("Капитаном команды может быть только студент");
            }

            ensureStudentNotInAnotherTeam(postId, captain.getId());
            members.add(captain);
        }

        TeamEntity team = TeamEntity.builder()
                .post(post)
                .name(request.getName().trim())
                .captain(captain)
                .createdAt(LocalDateTime.now())
                .members(members)
                .build();

        TeamEntity saved = teamRepository.save(team);
        return mapToTeam(saved);
    }

    @Override
    public Team getTeamById(UUID postId, UUID teamId) {
        User currentUser = meService.getMe();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может просматривать команду");
        }

        PostEntity post = getTeamTaskPost(postId);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Команда не найдена"));

        if (!team.getPost().getId().equals(post.getId())) {
            throw new NotFoundException("Команда не принадлежит этому заданию");
        }

        return mapToTeam(team);
    }

    @Override
    @Transactional
    public Team updateTeam(UUID postId, UUID teamId, TeamUpdateRequest request) {
        User currentUser = meService.getMe();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может редактировать команды");
        }

        if (request == null) {
            throw new BadRequestException("Тело запроса обязательно");
        }

        PostEntity post = getTeamTaskPost(postId);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Команда не найдена"));

        if (!team.getPost().getId().equals(post.getId())) {
            throw new NotFoundException("Команда не принадлежит этому заданию");
        }

        if (request.getName() != null) {
            String trimmedName = request.getName().trim();
            if (trimmedName.isEmpty()) {
                throw new BadRequestException("Название команды не может быть пустым");
            }
            team.setName(trimmedName);
        }

        if (request.getCaptainId() != null) {
            UserEntity captain = userRepository.findById(request.getCaptainId())
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + request.getCaptainId()));

            if (captain.getRole() != Role.STUDENT) {
                throw new BadRequestException("Капитаном команды может быть только студент");
            }

            boolean isMember = team.getMembers() != null &&
                    team.getMembers().stream().anyMatch(member -> member.getId().equals(captain.getId()));

            if (!isMember) {
                throw new BadRequestException("Капитан должен быть участником команды");
            }

            team.setCaptain(captain);
        }

        TeamEntity saved = teamRepository.save(team);
        return mapToTeam(saved);
    }

    @Override
    @Transactional
    public void deleteTeam(UUID postId, UUID teamId) {
        User currentUser = meService.getMe();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может удалять команды");
        }

        PostEntity post = getTeamTaskPost(postId);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Команда не найдена"));

        if (!team.getPost().getId().equals(post.getId())) {
            throw new NotFoundException("Команда не принадлежит этому заданию");
        }

        teamRepository.delete(team);
    }

    @Override
    @Transactional
    public Team addStudentToTeam(UUID postId, UUID teamId, UUID studentId) {
        User currentUser = meService.getMe();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может добавлять участников в команду");
        }

        PostEntity post = getTeamTaskPost(postId);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Команда не найдена"));

        if (!team.getPost().getId().equals(post.getId())) {
            throw new NotFoundException("Команда не принадлежит этому заданию");
        }

        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new BadRequestException("Участником команды может быть только студент");
        }

        ensureStudentCanBeAddedToTeam(postId, teamId, studentId);

        if (team.getMembers() == null) {
            team.setMembers(new java.util.LinkedHashSet<>());
        }

        team.getMembers().add(student);

        TeamEntity saved = teamRepository.save(team);
        return mapToTeam(saved);
    }

    @Override
    @Transactional
    public void removeStudentFromTeam(UUID postId, UUID teamId, UUID studentId) {
        User currentUser = meService.getMe();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может удалять участников из команды");
        }

        PostEntity post = getTeamTaskPost(postId);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Команда не найдена"));

        if (!team.getPost().getId().equals(post.getId())) {
            throw new NotFoundException("Команда не принадлежит этому заданию");
        }

        if (team.getMembers() == null || team.getMembers().isEmpty()) {
            throw new BadRequestException("В команде нет участников");
        }

        UserEntity memberToRemove = team.getMembers().stream()
                .filter(member -> member.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Студент не состоит в этой команде"));

        team.getMembers().remove(memberToRemove);

        if (team.getCaptain() != null && team.getCaptain().getId().equals(studentId)) {
            team.setCaptain(null);
        }

        teamRepository.save(team);
    }

    private void ensureStudentCanBeAddedToTeam(UUID postId, UUID teamId, UUID studentId) {
        List<TeamEntity> teams = teamRepository.findAllByPost_IdOrderByCreatedAtAsc(postId);

        for (TeamEntity existingTeam : teams) {
            boolean alreadyMember = existingTeam.getMembers() != null &&
                    existingTeam.getMembers().stream().anyMatch(member -> member.getId().equals(studentId));

            if (!alreadyMember) {
                continue;
            }

            if (existingTeam.getId().equals(teamId)) {
                throw new BadRequestException("Студент уже состоит в этой команде");
            }

            throw new BadRequestException("Студент уже состоит в другой команде этого задания");
        }
    }

    private PostEntity getTeamTaskPost(UUID postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Пост не найден"));

        if (post.getType() != PostType.TASK || post.getTask() == null) {
            throw new NotFoundException("Задание не найдено");
        }

        if (post.getTask().getMode() != TaskMode.TEAM) {
            throw new NotFoundException("Для данного задания команды недоступны");
        }

        return post;
    }

    private void validateCreateRequest(TeamCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Тело запроса обязательно");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Название команды обязательно");
        }
    }

    private void ensureStudentNotInAnotherTeam(UUID postId, UUID studentId) {
        List<TeamEntity> teams = teamRepository.findAllByPost_IdOrderByCreatedAtAsc(postId);

        for (TeamEntity team : teams) {
            boolean alreadyMember = team.getMembers() != null &&
                    team.getMembers().stream().anyMatch(member -> member.getId().equals(studentId));

            if (alreadyMember) {
                throw new BadRequestException("Студент уже состоит в другой команде этого задания");
            }
        }
    }

    private TeamSummary mapToSummary(TeamEntity team) {
        TeamSummary dto = new TeamSummary();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setMembersCount(team.getMembers() != null ? team.getMembers().size() : 0);
        dto.setCaptainId(team.getCaptain() != null ? team.getCaptain().getId() : null);
        return dto;
    }

    private Team mapToTeam(TeamEntity team) {
        Team dto = new Team();
        dto.setId(team.getId());
        dto.setPostId(team.getPost().getId());
        dto.setName(team.getName());
        dto.setCaptainId(team.getCaptain() != null ? team.getCaptain().getId() : null);
        dto.setCreatedAt(team.getCreatedAt());

        List<StudentSummaryDto> members = new ArrayList<>();
        if (team.getMembers() != null) {
            for (UserEntity member : team.getMembers()) {
                StudentSummaryDto student = new StudentSummaryDto(
                        member.getId(),
                        null
                );
                members.add(student);
            }
        }

        dto.setMembers(members);
        return dto;
    }
}
