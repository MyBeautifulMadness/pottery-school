package com.example.PotteryPotSchool.service.Solutions.impl;

import com.example.PotteryPotSchool.config.*;
import com.example.PotteryPotSchool.dto.Solutions.*;
import com.example.PotteryPotSchool.dto.Teams.Team;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.entity.Solutions.*;
import com.example.PotteryPotSchool.entity.Teams.TeamEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Posts.PrioritySolution;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import com.example.PotteryPotSchool.enums.Solutions.*;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.*;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Login.JwtService;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Solutions.*;
import com.example.PotteryPotSchool.service.Teams.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolutionServiceImpl implements SolutionService {

    private final SolutionRepository solutionRepository;
    private final SolutionVoteRepository voteRepository;
    private final PostRepository postRepository;
    private final MeService meService;
    private final SolutionMapper solutionMapper;
    private final JwtService jwtService;
    private final TeamService teamService;

    @Override
    public Solution create(UUID postId, SolutionCreateRequest request) {

        User user = meService.getMe();
        ensureStudent(user);

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (post.getType() != PostType.TASK) {
            throw new BadRequestException("Only TASK");
        }

        TaskEntity task = post.getTask();
        boolean isTeam = task.getMode() == TaskMode.TEAM;

        if (isTeam) {
            UUID myTeamId = teamService.getMyTeam(postId).getId();
            if (!myTeamId.equals(request.getTeamId())) {
                throw new ForbiddenException("Wrong team");
            }
        } else {
            if (solutionRepository.existsByPostIdAndStudentId(postId, user.getId())) {
                throw new BadRequestException("Already exists");
            }
        }

        LocalDateTime now = LocalDateTime.now();

        SolutionEntity solution = SolutionEntity.builder()
                .post(post)
                .studentId(user.getId())
                .teamId(request.getTeamId())
                .ownerType(isTeam ? SolutionOwnerType.TEAM : SolutionOwnerType.STUDENT)
                .status(SolutionStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();

        apply(solution, request);

        if (Boolean.TRUE.equals(request.getSubmit())) {
            solution.setStatus(SolutionStatus.SUBMITTED);
            solution.setSubmittedAt(now);
        }

        return solutionMapper.toDto(solutionRepository.save(solution));
    }

    @Override
    public Solution getMySolution(UUID postId, UUID studentId) {
        Optional<SolutionEntity> solution = solutionRepository.findByPostIdAndStudentId(postId, studentId);
        if(solution.isPresent()){
            return solutionMapper.toDto(solution.get());
        }
        else return null;
    }

    public List<SolutionSummaryDto> getTeamSolutions(UUID postId, UUID studentId) {

        Team team = teamService.getMyTeam(postId);

        return solutionRepository.findByPostId(postId)
                .stream()
                .filter(s -> team.getMembers().stream()
                        .anyMatch(m -> m.getId().equals(s.getStudentId())))
                .map(solutionMapper::toSummaryDto)
                .toList();
    }

    @Override
    public Solution getSelected(UUID postId) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        Team team = teamService.getMyTeam(postId);

        PrioritySolution priority = post.getTask().getPrioritySolution();

        List<SolutionEntity> solutions = solutionRepository.findByPostIdAndStatus(
                postId, SolutionStatus.SUBMITTED
        );

        SolutionEntity result;

        switch (priority) {

            case CAPITAIN -> {

                if (team.getCaptainId() == null) {
                    result = null;
                    break;
                }

                UUID captainId = team.getCaptainId();

                result = solutions.stream()
                        .filter(s -> captainId.equals(s.getStudentId()))
                        .findFirst()
                        .orElse(null);
            }

            case FIRST -> result = solutions.stream()
                    .min(Comparator.comparing(SolutionEntity::getSubmittedAt))
                    .orElse(null);

            case LAST -> result = solutions.stream()
                    .max(Comparator.comparing(SolutionEntity::getSubmittedAt))
                    .orElse(null);

            case VOTING -> result = solutions.stream()
                    .max(Comparator.comparing(s ->
                            voteRepository.countBySolutionId(s.getId())
                    ))
                    .orElse(null);

            default -> result = null;
        }

        if (result == null) {
            throw new NotFoundException("Not found");
        }

        return solutionMapper.toDto(result);
    }

    @Override
    public Solution update(UUID solutionId, SolutionUpdateRequest request) {

        User user = meService.getMe();
        ensureStudent(user);

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        if (!solution.getStudentId().equals(user.getId())) {
            throw new ForbiddenException("Forbidden");
        }

        apply(solution, request);
        solution.setUpdatedAt(LocalDateTime.now());

        return solutionMapper.toDto(solutionRepository.save(solution));
    }

    @Override
    public Solution submit(UUID solutionId) {

        User user = meService.getMe();
        ensureStudent(user);

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        if (!solution.getStudentId().equals(user.getId())) {
            throw new ForbiddenException("Forbidden");
        }

        LocalDateTime now = LocalDateTime.now();
        solution.setStatus(SolutionStatus.SUBMITTED);
        solution.setSubmittedAt(now);
        solution.setUpdatedAt(now);

        return solutionMapper.toDto(solutionRepository.save(solution));
    }

    @Override
    public SolutionDetailsDto getSolution(UserPrincipal user, UUID solutionId) {

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        if (user.getRole() == Role.STUDENT &&
                !solution.getStudentId().equals(user.getId())) {
            throw new ForbiddenException("Forbidden");
        }

        return solutionMapper.toDetailsDto(solution);
    }

    @Override
    public List<SolutionSummaryDto> getSolutions(
            UUID postId,
            SolutionStatus status,
            SolutionOwnerType ownerType,
            UUID teamId,
            UUID studentId,
            UUID authorStudentId,
            Boolean selectedOnly,
            UserPrincipal user
    ) {

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only teacher");
        }

        return solutionRepository.findByPostId(postId)
                .stream()
                .filter(s -> status == null || s.getStatus() == status)
                .filter(s -> ownerType == null || s.getOwnerType() == ownerType)
                .filter(s -> teamId == null || teamId.equals(s.getTeamId()))
                .filter(s -> studentId == null || studentId.equals(s.getStudentId()))
                .filter(s -> authorStudentId == null || authorStudentId.equals(s.getStudentId()))
                .filter(s -> selectedOnly == null || !selectedOnly || s.getStatus() == SolutionStatus.SUBMITTED)
                .map(solutionMapper::toSummaryDto)
                .toList();
    }

    @Override
    public Solution vote(UUID solutionId) {

        User user = meService.getMe();

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        UUID postId = solution.getPost().getId();

        voteRepository.deleteByPostIdAndStudentId(postId, user.getId());

        SolutionVote vote = SolutionVote.builder()
                .solutionId(solutionId)
                .studentId(user.getId())
                .postId(postId)
                .build();

        voteRepository.save(vote);

        return solutionMapper.toDto(solution);
    }

    @Override
    public Solution unvote(UUID solutionId) {

        User user = meService.getMe();

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        voteRepository.deleteByPostIdAndStudentId(
                solution.getPost().getId(),
                user.getId()
        );

        return solutionMapper.toDto(solution);
    }

    private void apply(SolutionEntity s, SolutionCreateRequest r) {
        s.setText(r.getText());
        s.setVideoUrl(r.getVideoUrl());
        s.setAttachmentUrl(r.getAttachmentUrl());
    }

    private void apply(SolutionEntity s, SolutionUpdateRequest r) {
        s.setText(r.getText());
        s.setVideoUrl(r.getVideoUrl());
        s.setAttachmentUrl(r.getAttachmentUrl());
    }

    private void ensureStudent(User user) {
        if (user.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only student");
        }
    }
}