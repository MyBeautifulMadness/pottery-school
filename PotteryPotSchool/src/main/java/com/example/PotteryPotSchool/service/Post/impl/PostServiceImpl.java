package com.example.PotteryPotSchool.service.Post.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Criteria.CriterionCreateRequest;
import com.example.PotteryPotSchool.dto.Criteria.CriterionDto;
import com.example.PotteryPotSchool.dto.Posts.*;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Grades.CriterionEntity;
import com.example.PotteryPotSchool.entity.Posts.MaterialEntity;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionVote;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Grades.CriterionImpactType;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Posts.PrioritySolution;
import com.example.PotteryPotSchool.enums.Posts.ReviewType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    private final CriterionRepository criterionRepository;
    private final SelfAssessmentItemRepository selfAssessmentItemRepository;
    private final CriterionGradeItemRepository criterionGradeItemRepository;
    private final PeerReviewRepository peerReviewRepository;

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

        criterionGradeItemRepository.deleteAllByGrade_Solution_Post_Id(postId);
        selfAssessmentItemRepository.deleteAllBySolution_Post_Id(postId);
        gradeRepository.deleteAllBySolution_Post_Id(postId);
        peerReviewRepository.deleteAllByPostId(postId);
        solutionRepository.deleteAllByPost_Id(postId);
        commentRepository.deleteAllByPostId(postId);
        if (post.getType() == PostType.TASK && post.getTask() != null) {
            teamRepository.deleteAllByPost_Id(postId);
        }
        criterionRepository.deleteAllByTask_Post_Id(postId);

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

        if (taskRequest.getGradingSettings() != null) {
            applyGradingSettings(task, taskRequest.getGradingSettings());
        }

        if (taskRequest.getReviewSettings() != null) {
            applyReviewSettings(task, taskRequest.getReviewSettings());
        }

        if (taskRequest.getCriteria() != null) {
            task.getCriteria().clear();
            taskRequest.getCriteria().forEach(criterionRequest -> task.getCriteria().add(mapToCriterionEntity(criterionRequest, task)));
        }

        if (Boolean.TRUE.equals(task.getGradingEnabled()) && (task.getCriteria() == null || task.getCriteria().isEmpty())) {
            throw new BadRequestException("Для оценивания по критериям нужно указать хотя бы один критерий");
        }

        validateUpdateRegularCriteriaScoreSum(task);
    }


    private void applyGradingSettings(TaskEntity task, TaskGradingSettings settings) {
        if (settings.getEnabled() != null) task.setGradingEnabled(settings.getEnabled());
        if (settings.getMaxFinalScore() != null) task.setMaxFinalScore(settings.getMaxFinalScore());
        if (settings.getSelfAssessmentRequired() != null) task.setSelfAssessmentRequired(settings.getSelfAssessmentRequired());
        if (settings.getLatePenaltyEnabled() != null) task.setLatePenaltyEnabled(settings.getLatePenaltyEnabled());
        if (settings.getLatePenaltyPerDay() != null) task.setLatePenaltyPerDay(settings.getLatePenaltyPerDay());
        if (settings.getProgressPenaltyEnabled() != null) task.setProgressPenaltyEnabled(settings.getProgressPenaltyEnabled());
        if (settings.getProgressPenaltyPerMiss() != null) task.setProgressPenaltyPerMiss(settings.getProgressPenaltyPerMiss());

        TaskGradingSettings resulting = mapToTaskGradingSettings(task);
        validateGradingSettingsValues(resulting);
    }

    private void applyReviewSettings(TaskEntity task, TaskReviewSettings settings) {
        ReviewType type = settings.getReviewType() != null ? settings.getReviewType() : task.getReviewType();
        validateReviewSettings(type, settings.getReviewsPerStudent(), settings.getReviewDeadline());
        task.setReviewType(type);
        if (type == ReviewType.PEER_TO_PEER) {
            task.setReviewsPerStudent(settings.getReviewsPerStudent());
            task.setReviewDeadline(settings.getReviewDeadline());
        } else {
            task.setReviewsPerStudent(null);
            task.setReviewDeadline(null);
        }
    }

    private void validateReviewSettings(ReviewType type, Integer reviewsPerStudent, LocalDateTime reviewDeadline) {
        if (type == ReviewType.PEER_TO_PEER) {
            if (reviewsPerStudent == null || reviewsPerStudent < 1) {
                throw new BadRequestException("Для peer-to-peer проверки укажите количество работ для проверки (>= 1)");
            }
            if (reviewDeadline == null) {
                throw new BadRequestException("Для peer-to-peer проверки укажите дедлайн проверки");
            }
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

        validateGradingSettings(task.getGradingSettings(), task.getCriteria());
        validateRegularCriteriaScoreSum(task.getGradingSettings(), task.getCriteria());

        if (task.getReviewSettings() != null && task.getReviewSettings().getReviewType() != null) {
            validateReviewSettings(
                    task.getReviewSettings().getReviewType(),
                    task.getReviewSettings().getReviewsPerStudent(),
                    task.getReviewSettings().getReviewDeadline());
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
        TaskGradingSettings gradingSettings = request.getGradingSettings();

        TaskEntity task = TaskEntity.builder()
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
                .gradingEnabled(gradingSettings != null && Boolean.TRUE.equals(gradingSettings.getEnabled()))
                .maxFinalScore(gradingSettings != null ? gradingSettings.getMaxFinalScore() : null)
                .selfAssessmentRequired(gradingSettings != null && Boolean.TRUE.equals(gradingSettings.getSelfAssessmentRequired()))
                .latePenaltyEnabled(gradingSettings != null && Boolean.TRUE.equals(gradingSettings.getLatePenaltyEnabled()))
                .latePenaltyPerDay(gradingSettings != null ? gradingSettings.getLatePenaltyPerDay() : null)
                .progressPenaltyEnabled(gradingSettings != null && Boolean.TRUE.equals(gradingSettings.getProgressPenaltyEnabled()))
                .progressPenaltyPerMiss(gradingSettings != null ? gradingSettings.getProgressPenaltyPerMiss() : null)
                .reviewType(request.getReviewSettings() != null && request.getReviewSettings().getReviewType() != null
                        ? request.getReviewSettings().getReviewType() : ReviewType.NORMAL)
                .reviewsPerStudent(request.getReviewSettings() != null ? request.getReviewSettings().getReviewsPerStudent() : null)
                .reviewDeadline(request.getReviewSettings() != null ? request.getReviewSettings().getReviewDeadline() : null)
                .post(post)
                .build();

        if (request.getCriteria() != null) {
            task.setCriteria(new ArrayList<>());
            request.getCriteria().forEach(criterionRequest -> task.getCriteria().add(mapToCriterionEntity(criterionRequest, task)));
        }

        return task;
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
            taskDetails.setGradingSettings(mapToTaskGradingSettings(post.getTask()));
            TaskReviewSettings reviewSettings = new TaskReviewSettings();
            reviewSettings.setReviewType(post.getTask().getReviewType());
            reviewSettings.setReviewsPerStudent(post.getTask().getReviewsPerStudent());
            reviewSettings.setReviewDeadline(post.getTask().getReviewDeadline());
            taskDetails.setReviewSettings(reviewSettings);
            taskDetails.setCriteria(post.getTask().getCriteria() == null ? List.of() : post.getTask().getCriteria().stream()
                    .sorted(Comparator.comparing(CriterionEntity::getDisplayOrder))
                    .map(this::mapToCriterionDto)
                    .toList());

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


    private CriterionEntity mapToCriterionEntity(CriterionCreateRequest request, TaskEntity task) {
        validateCriterionRequest(request);
        return CriterionEntity.builder()
                .task(task)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .type(request.getType())
                .maxScore(request.getMaxScore())
                .impactType(request.getImpactType())
                .displayOrder(request.getDisplayOrder())
                .build();
    }

    private CriterionDto mapToCriterionDto(CriterionEntity entity) {
        return CriterionDto.builder()
                .id(entity.getId())
                .postId(entity.getTask().getPost().getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .type(entity.getType())
                .maxScore(entity.getMaxScore())
                .impactType(entity.getImpactType())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }

    private TaskGradingSettings mapToTaskGradingSettings(TaskEntity task) {
        TaskGradingSettings settings = new TaskGradingSettings();
        settings.setEnabled(Boolean.TRUE.equals(task.getGradingEnabled()));
        settings.setMaxFinalScore(task.getMaxFinalScore());
        settings.setSelfAssessmentRequired(Boolean.TRUE.equals(task.getSelfAssessmentRequired()));
        settings.setLatePenaltyEnabled(Boolean.TRUE.equals(task.getLatePenaltyEnabled()));
        settings.setLatePenaltyPerDay(task.getLatePenaltyPerDay());
        settings.setProgressPenaltyEnabled(Boolean.TRUE.equals(task.getProgressPenaltyEnabled()));
        settings.setProgressPenaltyPerMiss(task.getProgressPenaltyPerMiss());
        return settings;
    }

    private void validateGradingSettingsValues(TaskGradingSettings settings) {
        if (settings == null || !Boolean.TRUE.equals(settings.getEnabled())) {
            return;
        }

        if (settings.getMaxFinalScore() == null
                || settings.getMaxFinalScore().compareTo(BigDecimal.ZERO) <= 0
                || settings.getMaxFinalScore().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("При оценивании по критериям maxFinalScore должен быть больше 0 и не больше 100");
        }

        if (Boolean.TRUE.equals(settings.getLatePenaltyEnabled())
                && (settings.getLatePenaltyPerDay() == null || settings.getLatePenaltyPerDay().compareTo(BigDecimal.ZERO) < 0)) {
            throw new BadRequestException("latePenaltyPerDay обязателен и не может быть отрицательным");
        }

        if (Boolean.TRUE.equals(settings.getProgressPenaltyEnabled())
                && (settings.getProgressPenaltyPerMiss() == null || settings.getProgressPenaltyPerMiss().compareTo(BigDecimal.ZERO) < 0)) {
            throw new BadRequestException("progressPenaltyPerMiss обязателен и не может быть отрицательным");
        }
    }

    private void validateGradingSettings(TaskGradingSettings settings, List<CriterionCreateRequest> criteria) {
        if (settings == null || !Boolean.TRUE.equals(settings.getEnabled())) {
            return;
        }

        validateGradingSettingsValues(settings);

        if (criteria == null || criteria.isEmpty()) {
            throw new BadRequestException("Для оценивания по критериям нужно указать хотя бы один критерий");
        }

        criteria.forEach(this::validateCriterionRequest);
    }

    private void validateCriterionRequest(CriterionCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Критерий не может быть null");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Название критерия обязательно");
        }
        if (request.getType() == null) {
            throw new BadRequestException("Тип критерия обязателен");
        }
        if (request.getImpactType() == null) {
            throw new BadRequestException("Тип влияния критерия обязателен");
        }
        if (request.getMaxScore() == null || request.getMaxScore().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("maxScore обязателен и не может быть отрицательным");
        }
        if (request.getDisplayOrder() == null || request.getDisplayOrder() < 0) {
            throw new BadRequestException("displayOrder обязателен и не может быть отрицательным");
        }
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


    private void validateRegularCriteriaScoreSum(TaskGradingSettings gradingSettings, List<CriterionCreateRequest> criteria) {

        if (gradingSettings == null || !Boolean.TRUE.equals(gradingSettings.getEnabled())) {
            return;
        }

        BigDecimal maxFinalScore = gradingSettings.getMaxFinalScore();

        if (maxFinalScore == null) {
            return;
        }

        BigDecimal regularCriteriaSum = criteria == null
                ? BigDecimal.ZERO
                : criteria.stream()
                .filter(criterion -> criterion.getImpactType() == CriterionImpactType.REGULAR)
                .map(CriterionCreateRequest::getMaxScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (regularCriteriaSum.compareTo(maxFinalScore) != 0) {
            throw new BadRequestException(
                    "Ошибка создания задания: Сумма оценок всех критериев должна равняться: "
                            + maxFinalScore
                            + ". Сейчас сумма обязательных критериев равняется: "
                            + regularCriteriaSum
            );
        }
    }

    private void validateUpdateRegularCriteriaScoreSum(TaskEntity task) {
        if (!Boolean.TRUE.equals(task.getGradingEnabled())) {
            return;
        }

        BigDecimal maxFinalScore = task.getMaxFinalScore();

        if (maxFinalScore == null) {
            return;
        }

        BigDecimal regularCriteriaSum = task.getCriteria() == null
                ? BigDecimal.ZERO
                : task.getCriteria().stream()
                .filter(criterion -> criterion.getImpactType() == CriterionImpactType.REGULAR)
                .map(CriterionEntity::getMaxScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (regularCriteriaSum.compareTo(maxFinalScore) != 0) {
            throw new BadRequestException(
                    "Ошибка создания задания: Сумма оценок всех критериев должна равняться: "
                            + maxFinalScore
                            + ". Сейчас сумма обязательных критериев равняется: "
                            + regularCriteriaSum
            );
        }
    }
}
