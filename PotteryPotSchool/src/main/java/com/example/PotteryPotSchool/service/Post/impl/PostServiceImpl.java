package com.example.PotteryPotSchool.service.Post.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.dto.Posts.*;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.MaterialEntity;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MeService meService;

    @Override
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
        return mapToPostDetails(savedPost);
    }

    private void validateCreateRequest(PostCreateRequest request) {
        if (request.getType() == PostType.MATERIAL) {
            if (request.getMaterial() == null || request.getTask() != null) {
                throw new BadRequestException("Публикация МАТЕРИАЛОВ должна содержать только материалы");
            }
        }

        if (request.getType() == PostType.TASK) {
            if (request.getTask() == null || request.getMaterial() != null) {
                throw new BadRequestException("Публикация ЗАДАНИЙ должна содержать только задания");
            }
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
        return TaskEntity.builder()
                .description(request.getDescription())
                .deadline(request.getDeadline())
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
            details.setTask(taskDetails);
        }

        return details;
    }
}
