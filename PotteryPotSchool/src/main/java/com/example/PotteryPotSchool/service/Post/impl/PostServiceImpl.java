package com.example.PotteryPotSchool.service.Post.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Posts.*;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.MaterialEntity;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionVote;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Posts.PrioritySolution;
import com.example.PotteryPotSchool.enums.Posts.TeamDistributionType;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.*;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Post.PostService;
import com.example.PotteryPotSchool.entity.Teams.TeamEntity;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MeService meService;
    private final CommentRepository commentRepository;
    private final SolutionRepository solutionRepository;
    private final GradeRepository gradeRepository;
    private final TeamRepository teamRepository;
    private final SolutionVoteRepository solutionVoteRepository;

    @Override
    @Transactional
    public PostDetails createPost(PostCreateRequest request) {
        User currentUser = meService.getMe();

        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только учителя могут создавать пост");
        }

        validateCreateRequest(request);

        LocalDateTime now = LocalDateTime.now();

        PostEntity post = PostEntity.builder()
                .type(request.getType())
                .title(request.getTitle())
                .description(request.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .build();

        if (request.getType() == PostType.MATERIAL) {
            MaterialEntity material = mapToMaterialEntity(request.getMaterial(), post);
            post.setMaterial(material);
        }

        if (request.getType() == PostType.TASK) {
            TaskEntity task = mapToTaskEntity(request.getTask(), post);
            post.setTask(task);
        }

        PostEntity savedPost = postRepository.save(post);

        autoCreateTeamsIfNeeded(savedPost);

        return mapToPostDetails(savedPost);
    }

    @Override
    @Transactional
    public void delete(UUID postId) {
        User currentUser = meService.getMe();

        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только учителя могут удалять пост");
        }

        PostEntity post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Пост не найден: " + postId));

        gradeRepository.deleteAllBySolution_Post_Id(postId);
        solutionRepository.deleteAllByPost_Id(postId);
        commentRepository.deleteAllByPostId(postId);
        if (post.getType() == PostType.TASK && post.getTask() != null) {
            teamRepository.deleteAllByPost_Id(postId);
        }

        postRepository.delete(post);
    }

    @Override
    public Paged<PostShortDetails> getPosts(PostType type, int page, int size) {
        meService.getMe();

        Pageable pageable = PageRequest.of(page, size);
        Page<PostEntity> postPage;

        if (type == null) {
            postPage = postRepository.findAll(pageable);
        } else {
            postPage = postRepository.findAllByType(type, pageable);
        }

        List<PostShortDetails> items = postPage.getContent().stream()
                .map(this::mapToPostShortDetails)
                .toList();


        return new Paged<>(
                items,
                page,
                size,
                postPage.getTotalElements()
        );
    }

    @Override
    public PostDetails getById(UUID postId) {
        meService.getMe();

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Пост не найден: " + postId));

        return mapToPostDetails(post);
    }

    @Override
    @Transactional
    public PostDetails update(UUID postId, PostUpdateRequest request) {
        User currentUser = meService.getMe();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может редактировать посты");
        }

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Пост не найден"));

        if (request == null) {
            throw new BadRequestException("Тело запроса обязательно");
        }

        applyPostBaseUpdate(post, request);

        if (post.getType() == PostType.MATERIAL) {
            applyMaterialUpdate(post, request);
        } else if (post.getType() == PostType.TASK) {
            applyTaskUpdate(post, request);
        }

        post.setUpdatedAt(LocalDateTime.now());

        PostEntity saved = postRepository.save(post);
        return mapToPostDetails(saved);
    }

    private void applyPostBaseUpdate(PostEntity post, PostUpdateRequest request) {
        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new BadRequestException("Название поста не может быть пустым");
            }
            post.setTitle(title);
        }

        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
        }
    }

    private void applyMaterialUpdate(PostEntity post, PostUpdateRequest request) {
        if (request.getTask() != null) {
            throw new BadRequestException("Для MATERIAL-поста нельзя обновлять task");
        }

        if (request.getMaterial() == null) {
            return;
        }

        if (post.getMaterial() == null) {
            throw new BadRequestException("У поста отсутствует material");
        }

        MaterialUpdateRequest materialRequest = request.getMaterial();

        if (materialRequest.getTitle() != null) {
            String title = materialRequest.getTitle().trim();
            if (title.isEmpty()) {
                throw new BadRequestException("Название материала не может быть пустым");
            }
            post.getMaterial().setTitle(title);
        }

        if (materialRequest.getUrl() != null) {
            post.getMaterial().setUrl(materialRequest.getUrl());
        }

        if (materialRequest.getText() != null) {
            post.getMaterial().setText(materialRequest.getText());
        }
    }

    private void applyTaskUpdate(PostEntity post, PostUpdateRequest request) {
        if (request.getMaterial() != null) {
            throw new BadRequestException("Для TASK-поста нельзя обновлять material");
        }

        if (request.getTask() == null) {
            return;
        }

        if (post.getTask() == null) {
            throw new BadRequestException("У поста отсутствует task");
        }

        TaskUpdateRequest taskRequest = request.getTask();
        TaskEntity task = post.getTask();

        TaskMode newMode = taskRequest.getMode() != null ? taskRequest.getMode() : task.getMode();
        TeamDistributionType newDistributionType =
                taskRequest.getTeamDistributionType() != null
                        ? taskRequest.getTeamDistributionType()
                        : task.getTeamDistributionType();
        PrioritySolution newPrioritySolution =
                taskRequest.getPrioritySolution() != null
                        ? taskRequest.getPrioritySolution()
                        : task.getPrioritySolution();

        TeamRules incomingRules = taskRequest.getTeamRules();

        LocalDateTime newFormationDeadline = incomingRules != null
                ? incomingRules.getFormationDeadline()
                : task.getFormationDeadline();

        Integer newMinTeamsCount = incomingRules != null
                ? incomingRules.getMinTeamsCount()
                : task.getMinTeamsCount();

        Integer newMaxTeamsCount = incomingRules != null
                ? incomingRules.getMaxTeamsCount()
                : task.getMaxTeamsCount();

        Integer newMinMembersPerTeam = incomingRules != null
                ? incomingRules.getMinMembersPerTeam()
                : task.getMinMembersPerTeam();

        Integer newMaxMembersPerTeam = incomingRules != null
                ? incomingRules.getMaxMembersPerTeam()
                : task.getMaxMembersPerTeam();

        validateTaskPatchState(
                newMode,
                newDistributionType,
                newPrioritySolution,
                newMinTeamsCount,
                newMaxTeamsCount,
                newMinMembersPerTeam,
                newMaxMembersPerTeam
        );

        if (taskRequest.getDescription() != null) {
            task.setDescription(taskRequest.getDescription());
        }

        if (taskRequest.getDeadline() != null) {
            task.setDeadline(taskRequest.getDeadline());
        }

        if (taskRequest.getMode() != null) {
            task.setMode(taskRequest.getMode());
        }

        if (taskRequest.getTeamDistributionType() != null) {
            task.setTeamDistributionType(taskRequest.getTeamDistributionType());
        }

        if (taskRequest.getPrioritySolution() != null) {
            task.setPrioritySolution(taskRequest.getPrioritySolution());
        }

        if (incomingRules != null) {
            task.setFormationDeadline(incomingRules.getFormationDeadline());
            task.setMinTeamsCount(incomingRules.getMinTeamsCount());
            task.setMaxTeamsCount(incomingRules.getMaxTeamsCount());
            task.setMinMembersPerTeam(incomingRules.getMinMembersPerTeam());
            task.setMaxMembersPerTeam(incomingRules.getMaxMembersPerTeam());
        }
    }

    private void validateTaskPatchState(
            TaskMode mode,
            TeamDistributionType teamDistributionType,
            PrioritySolution prioritySolution,
            Integer minTeamsCount,
            Integer maxTeamsCount,
            Integer minMembersPerTeam,
            Integer maxMembersPerTeam
    ) {
        if (mode == null) {
            throw new BadRequestException("Для задания mode обязателен");
        }

        if (mode == TaskMode.SOLO) {
            if (teamDistributionType != null || prioritySolution != null
                    || minTeamsCount != null || maxTeamsCount != null
                    || minMembersPerTeam != null || maxMembersPerTeam != null) {
                throw new BadRequestException(
                        "Для SOLO задания teamDistributionType, prioritySolution и teamRules должны быть null"
                );
            }
            return;
        }

        if (teamDistributionType == null) {
            throw new BadRequestException("Для TEAM задания teamDistributionType обязателен");
        }

        if (prioritySolution == null) {
            throw new BadRequestException("Для TEAM задания prioritySolution обязателен");
        }

        if (minTeamsCount != null && maxTeamsCount != null && minTeamsCount > maxTeamsCount) {
            throw new BadRequestException("minTeamsCount не может быть больше maxTeamsCount");
        }

        if (minMembersPerTeam != null && maxMembersPerTeam != null
                && minMembersPerTeam > maxMembersPerTeam) {
            throw new BadRequestException("minMembersPerTeam не может быть больше maxMembersPerTeam");
        }
    }

    private UUID resolveSelectedSolutionId(PostEntity post) {
        if (post.getType() != PostType.TASK || post.getTask() == null) {
            return null;
        }

        TaskEntity task = post.getTask();

        if (task.getSelectedSolutionId() != null) {
            return task.getSelectedSolutionId();
        }

        if (task.getMode() != TaskMode.TEAM) {
            return null;
        }

        if (task.getPrioritySolution() == null) {
            return null;
        }

        List<SolutionEntity> solutions = solutionRepository.findAllByPost_Id(post.getId());

        if (solutions == null || solutions.isEmpty()) {
            return null;
        }

        List<SolutionEntity> submittedSolutions = solutions.stream()
                .filter(solution -> solution.getStatus() == SolutionStatus.SUBMITTED)
                .toList();

        if (submittedSolutions.isEmpty()) {
            return null;
        }

        return switch (task.getPrioritySolution()) {
            case FIRST -> submittedSolutions.stream()
                    .sorted(java.util.Comparator.comparing(SolutionEntity::getCreatedAt))
                    .map(SolutionEntity::getId)
                    .findFirst()
                    .orElse(null);

            case LAST -> submittedSolutions.stream()
                    .sorted(java.util.Comparator.comparing(SolutionEntity::getCreatedAt).reversed())
                    .map(SolutionEntity::getId)
                    .findFirst()
                    .orElse(null);

            case CAPITAIN -> resolveCaptainSolutionId(post, submittedSolutions);

            case VOTING -> resolveVotingSolutionId(post, submittedSolutions);
        };
    }

    private UUID resolveVotingSolutionId(PostEntity post, List<SolutionEntity> submittedSolutions) {
        List<SolutionVote> votes = solutionVoteRepository.findAllByPostId(post.getId());

        if (votes == null || votes.isEmpty()) {
            return null;
        }

        java.util.Map<UUID, Long> votesBySolutionId = votes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        SolutionVote::getSolutionId,
                        java.util.stream.Collectors.counting()
                ));

        return submittedSolutions.stream()
                .sorted(
                        java.util.Comparator
                                .comparing(
                                        (SolutionEntity solution) -> votesBySolutionId.getOrDefault(solution.getId(), 0L)
                                )
                                .reversed()
                                .thenComparing(SolutionEntity::getCreatedAt)
                )
                .map(SolutionEntity::getId)
                .findFirst()
                .orElse(null);
    }

    private UUID resolveCaptainSolutionId(PostEntity post, List<SolutionEntity> submittedSolutions) {
        List<TeamEntity> teams = teamRepository.findAllByPost_IdOrderByCreatedAtAsc(post.getId());

        java.util.Set<UUID> captainIds = teams.stream()
                .map(TeamEntity::getCaptain)
                .filter(java.util.Objects::nonNull)
                .map(UserEntity::getId)
                .collect(java.util.stream.Collectors.toSet());

        if (captainIds.isEmpty()) {
            return null;
        }

        return submittedSolutions.stream()
                .filter(solution -> solution.getStudentId() != null
                        && captainIds.contains(solution.getStudentId()))
                .sorted(java.util.Comparator.comparing(SolutionEntity::getCreatedAt))
                .map(SolutionEntity::getId)
                .findFirst()
                .orElse(null);
    }



    private void validateCreateRequest(PostCreateRequest request) {
        if (request.getType() == PostType.MATERIAL) {
            if (request.getMaterial() == null || request.getTask() != null) {
                throw new BadRequestException("Публикация МАТЕРИАЛОВ должна содержать только материалы");
            }
            return;
        }

        if (request.getType() == PostType.TASK) {
            if (request.getTask() == null || request.getMaterial() != null) {
                throw new BadRequestException("Публикация ЗАДАНИЙ должна содержать только задания");
            }

            validateTaskRequest(request.getTask());
        }
    }

    private void validateTaskRequest(TaskCreateRequest task) {
        if (task.getMode() == null) {
            throw new BadRequestException("Для задания mode обязателен");
        }

        if (task.getMode() == com.example.PotteryPotSchool.enums.Posts.TaskMode.SOLO) {
            if (task.getTeamDistributionType() != null ||
                    task.getTeamRules() != null ||
                    task.getPrioritySolution() != null) {
                throw new BadRequestException("Для SOLO задания teamDistributionType, teamRules и prioritySolution должны быть null");
            }
            return;
        }

        if (task.getMode() == com.example.PotteryPotSchool.enums.Posts.TaskMode.TEAM) {
            if (task.getTeamDistributionType() == null) {
                throw new BadRequestException("Для TEAM задания teamDistributionType обязателен");
            }
            if (task.getPrioritySolution() == null) {
                throw new BadRequestException("Для TEAM задания prioritySolution обязателен");
            }
            validateTeamRules(task.getTeamRules());
        }
    }

    private void validateTeamRules(TeamRules rules) {
        if (rules == null) {
            return;
        }

        if (rules.getMinTeamsCount() != null && rules.getMaxTeamsCount() != null
                && rules.getMinTeamsCount() > rules.getMaxTeamsCount()) {
            throw new BadRequestException("minTeamsCount не может быть больше maxTeamsCount");
        }

        if (rules.getMinMembersPerTeam() != null && rules.getMaxMembersPerTeam() != null
                && rules.getMinMembersPerTeam() > rules.getMaxMembersPerTeam()) {
            throw new BadRequestException("minMembersPerTeam не может быть больше maxMembersPerTeam");
        }
    }

    private MaterialEntity mapToMaterialEntity(MaterialCreateRequest request, PostEntity post) {
        return MaterialEntity.builder()
                .type(request.getType())
                .title(request.getTitle())
                .url(request.getUrl())
                .text(request.getText())
                .post(post)
                .build();
    }

    private TaskEntity mapToTaskEntity(TaskCreateRequest request, PostEntity post) {
        TeamRules rules = request.getTeamRules();

        return TaskEntity.builder()
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .mode(request.getMode())
                .teamDistributionType(request.getTeamDistributionType())
                .formationDeadline(rules != null ? rules.getFormationDeadline() : null)
                .minTeamsCount(rules != null ? rules.getMinTeamsCount() : null)
                .maxTeamsCount(rules != null ? rules.getMaxTeamsCount() : null)
                .minMembersPerTeam(rules != null ? rules.getMinMembersPerTeam() : null)
                .maxMembersPerTeam(rules != null ? rules.getMaxMembersPerTeam() : null)
                .prioritySolution(request.getPrioritySolution())
                .selectedSolutionId(null)
                .post(post)
                .build();
    }

    private PostDetails mapToPostDetails(PostEntity post) {
        PostDetails details = new PostDetails();
        details.setId(post.getId());
        details.setType(post.getType());
        details.setTitle(post.getTitle());
        details.setDescription(post.getDescription());
        details.setCreatedAt(post.getCreatedAt());
        details.setUpdatedAt(post.getUpdatedAt());

        if (post.getMaterial() != null) {
            Material material = new Material();
            material.setType(post.getMaterial().getType());
            material.setTitle(post.getMaterial().getTitle());
            material.setUrl(post.getMaterial().getUrl());
            material.setText(post.getMaterial().getText());
            details.setMaterial(material);
        }

        if (post.getTask() != null) {
            TaskDetails taskDetails = new TaskDetails();
            taskDetails.setDescription(post.getTask().getDescription());
            taskDetails.setDeadline(post.getTask().getDeadline());
            taskDetails.setMode(post.getTask().getMode());
            taskDetails.setTeamDistributionType(post.getTask().getTeamDistributionType());
            taskDetails.setPrioritySolution(post.getTask().getPrioritySolution());
            taskDetails.setSelectedSolutionId(resolveSelectedSolutionId(post));

            TeamRules rules = new TeamRules();
            rules.setFormationDeadline(post.getTask().getFormationDeadline());
            rules.setMinTeamsCount(post.getTask().getMinTeamsCount());
            rules.setMaxTeamsCount(post.getTask().getMaxTeamsCount());
            rules.setMinMembersPerTeam(post.getTask().getMinMembersPerTeam());
            rules.setMaxMembersPerTeam(post.getTask().getMaxMembersPerTeam());

            if (rules.getFormationDeadline() != null ||
                    rules.getMinTeamsCount() != null ||
                    rules.getMaxTeamsCount() != null ||
                    rules.getMinMembersPerTeam() != null ||
                    rules.getMaxMembersPerTeam() != null) {
                taskDetails.setTeamRules(rules);
            }

            details.setTask(taskDetails);
        }

        return details;
    }

    private PostShortDetails mapToPostShortDetails(PostEntity post) {
        PostShortDetails details = new PostShortDetails();
        details.setId(post.getId());
        details.setType(post.getType());
        details.setTitle(post.getTitle());
        details.setDescription(post.getDescription());
        details.setCreatedAt(post.getCreatedAt());
        details.setUpdatedAt(post.getUpdatedAt());
        return details;
    }

    private void autoCreateTeamsIfNeeded(PostEntity post) {
        if (post.getType() != PostType.TASK || post.getTask() == null) {
            return;
        }

        TaskEntity task = post.getTask();

        if (task.getMode() != TaskMode.TEAM) {
            return;
        }

        Integer maxTeamsCount = task.getMaxTeamsCount();
        if (maxTeamsCount == null || maxTeamsCount <= 0) {
            return;
        }

        long existingTeams = teamRepository.countByPost_Id(post.getId());
        if (existingTeams > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= maxTeamsCount; i++) {
            TeamEntity team = TeamEntity.builder()
                    .post(post)
                    .name("Team " + i)
                    .captain(null)
                    .createdAt(now)
                    .build();

            teamRepository.save(team);
        }
    }
}
