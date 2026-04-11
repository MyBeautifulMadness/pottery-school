package com.example.PotteryPotSchool.service.Post.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Posts.*;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.MaterialEntity;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.CommentRepository;
import com.example.PotteryPotSchool.repository.GradeRepository;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Post.PostService;
import com.example.PotteryPotSchool.entity.Teams.TeamEntity;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import com.example.PotteryPotSchool.repository.TeamRepository;
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
    public PostDetails update(UUID postId, PostUpdateRequest request) {
        User currentUser = meService.getMe();

        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только учителя могут изменять пост");
        }

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Пост не найден: " + postId));

        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());

        if (post.getType() == PostType.MATERIAL) {
            if (request.getTask() != null) {
                throw new BadRequestException("Вы не можете изменить task для MATERIAL поста");
            }

            if (request.getMaterial() != null) {
                post.getMaterial().setTitle(request.getMaterial().getTitle());
                post.getMaterial().setUrl(request.getMaterial().getUrl());
                post.getMaterial().setText(request.getMaterial().getText());
            }
        }

        if (post.getType() == PostType.TASK) {
            if (request.getMaterial() != null) {
                throw new BadRequestException("Вы не можете изменить material для TASK поста");
            }

            if (request.getTask() != null) {
                validateTaskUpdateRequest(request.getTask());

                post.getTask().setDescription(request.getTask().getDescription());
                post.getTask().setDeadline(request.getTask().getDeadline());
                post.getTask().setMode(request.getTask().getMode());
                post.getTask().setTeamDistributionType(request.getTask().getTeamDistributionType());
                post.getTask().setPrioritySolution(request.getTask().getPrioritySolution());

                TeamRules rules = request.getTask().getTeamRules();
                post.getTask().setFormationDeadline(rules != null ? rules.getFormationDeadline() : null);
                post.getTask().setMinTeamsCount(rules != null ? rules.getMinTeamsCount() : null);
                post.getTask().setMaxTeamsCount(rules != null ? rules.getMaxTeamsCount() : null);
                post.getTask().setMinMembersPerTeam(rules != null ? rules.getMinMembersPerTeam() : null);
                post.getTask().setMaxMembersPerTeam(rules != null ? rules.getMaxMembersPerTeam() : null);
            }
        }

        post.setUpdatedAt(LocalDateTime.now());

        PostEntity savedPost = postRepository.save(post);
        return mapToPostDetails(savedPost);
    }

    private void validateTaskUpdateRequest(TaskUpdateRequest task) {
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

        if (task.getTeamDistributionType() == null) {
            throw new BadRequestException("Для TEAM задания teamDistributionType обязателен");
        }
        if (task.getPrioritySolution() == null) {
            throw new BadRequestException("Для TEAM задания prioritySolution обязателен");
        }

        validateTeamRules(task.getTeamRules());
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
            taskDetails.setSelectedSolutionId(post.getTask().getSelectedSolutionId());

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
