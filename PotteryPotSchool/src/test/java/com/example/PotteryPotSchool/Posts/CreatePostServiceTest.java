package com.example.PotteryPotSchool.Posts;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Posts.MaterialCreateRequest;
import com.example.PotteryPotSchool.dto.Posts.PostCreateRequest;
import com.example.PotteryPotSchool.dto.Posts.PostDetails;
import com.example.PotteryPotSchool.dto.Posts.TaskCreateRequest;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.MaterialEntity;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.enums.Posts.MaterialType;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Post.PostService;
import com.example.PotteryPotSchool.service.Post.impl.PostServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreatePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void shouldCreateMaterialPostWhenUserIsTeacher() {

        PostCreateRequest request = new PostCreateRequest();
        request.setType(PostType.MATERIAL);
        request.setTitle("Основа");
        request.setDescription("Вводный материал");

        MaterialCreateRequest materialRequest = new MaterialCreateRequest();
        materialRequest.setType(MaterialType.TEXT);
        materialRequest.setTitle("Теория");
        materialRequest.setText("Сам материал");
        request.setMaterial(materialRequest);

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@lol.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);

        UUID postId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity post = invocation.getArgument(0);
            post.setId(postId);
            post.setCreatedAt(now);
            post.setUpdatedAt(now);

            if (post.getMaterial() != null) {
                post.getMaterial().setId(UUID.randomUUID());
                post.getMaterial().setPost(post);
            }

            return post;
        });

        PostDetails result = postService.createPost(request);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals(PostType.MATERIAL, result.getType());
        assertEquals("Основа", result.getTitle());
        assertEquals("Вводный материал", result.getDescription());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());

        assertNotNull(result.getMaterial());
        assertEquals(MaterialType.TEXT, result.getMaterial().getType());
        assertEquals("Теория", result.getMaterial().getTitle());
        assertEquals("Сам материал", result.getMaterial().getText());
        assertNull(result.getTask());

        ArgumentCaptor<PostEntity> postCaptor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(postCaptor.capture());

        PostEntity savedPost = postCaptor.getValue();
        assertEquals(PostType.MATERIAL, savedPost.getType());
        assertEquals("Основа", savedPost.getTitle());
        assertEquals("Вводный материал", savedPost.getDescription());
        assertNotNull(savedPost.getMaterial());
        assertNull(savedPost.getTask());

        MaterialEntity savedMaterial = savedPost.getMaterial();
        assertEquals(MaterialType.TEXT, savedMaterial.getType());
        assertEquals("Теория", savedMaterial.getTitle());
        assertEquals("Сам материал", savedMaterial.getText());
    }


    @Test
    void shouldCreateTaskPostWhenUserIsTeacher() {

        PostCreateRequest request = new PostCreateRequest();
        request.setType(PostType.TASK);
        request.setTitle("Слепить горшок");
        request.setDescription("Домашнее задание");

        TaskCreateRequest taskRequest = new TaskCreateRequest();
        taskRequest.setDescription("Сделать горшок как-то...");
        taskRequest.setDeadline(LocalDateTime.of(2026, 3, 15, 18, 0));
        request.setTask(taskRequest);

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@lol.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);

        UUID postId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity post = invocation.getArgument(0);
            post.setId(postId);
            post.setCreatedAt(now);
            post.setUpdatedAt(now);

            if (post.getTask() != null) {
                post.getTask().setId(UUID.randomUUID());
                post.getTask().setPost(post);
            }

            return post;
        });

        PostDetails result = postService.createPost(request);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals(PostType.TASK, result.getType());
        assertEquals("Слепить горшок", result.getTitle());
        assertEquals("Домашнее задание", result.getDescription());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());

        assertNull(result.getMaterial());
        assertNotNull(result.getTask());
        assertEquals("Сделать горшок как-то...", result.getTask().getDescription());
        assertEquals(LocalDateTime.of(2026, 3, 15, 18, 0), result.getTask().getDeadline());

        ArgumentCaptor<PostEntity> postCaptor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(postCaptor.capture());

        PostEntity savedPost = postCaptor.getValue();
        assertEquals(PostType.TASK, savedPost.getType());
        assertEquals("Слепить горшок", savedPost.getTitle());
        assertEquals("Домашнее задание", savedPost.getDescription());
        assertNull(savedPost.getMaterial());
        assertNotNull(savedPost.getTask());

        TaskEntity savedTask = savedPost.getTask();
        assertEquals("Сделать горшок как-то...", savedTask.getDescription());
        assertEquals(LocalDateTime.of(2026, 3, 15, 18, 0), savedTask.getDeadline());
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {

        PostCreateRequest request = new PostCreateRequest();
        request.setType(PostType.MATERIAL);
        request.setTitle("Супер название");

        when(meService.getMe()).thenThrow(new UnauthorizedException("Unauthorized"));

        assertThrows(UnauthorizedException.class, () -> postService.createPost(request));

        verify(postRepository, never()).save(any(PostEntity.class));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUserIsNotTeacher() {
        PostCreateRequest request = new PostCreateRequest();
        request.setType(PostType.MATERIAL);
        request.setTitle("Супер название");

        User student = User.builder()
                .id(UUID.randomUUID())
                .email("student@lol.com")
                .role(Role.STUDENT)
                .build();

        when(meService.getMe()).thenReturn(student);
        assertThrows(ForbiddenException.class, () -> postService.createPost(request));

        verify(postRepository, never()).save(any(PostEntity.class));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenTypeIsMaterialButTaskProvided() {
        PostCreateRequest request = new PostCreateRequest();
        request.setType(PostType.MATERIAL);
        request.setTitle("Пост task, но статус MATERIAL");

        TaskCreateRequest taskRequest = new TaskCreateRequest();
        taskRequest.setDescription("чтото...");
        request.setTask(taskRequest);

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@lol.com")
                .role(Role.TEACHER)
                .build();

        when(meService.getMe()).thenReturn(teacher);
        assertThrows(BadRequestException.class, () -> postService.createPost(request));

        verify(postRepository, never()).save(any(PostEntity.class));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenTypeIsTaskButMaterialProvided() {

        PostCreateRequest request = new PostCreateRequest();
        request.setType(PostType.TASK);
        request.setTitle("Пост material, но статус TASK");

        MaterialCreateRequest materialRequest = new MaterialCreateRequest();
        materialRequest.setType(MaterialType.LINK);
        materialRequest.setTitle("чтото...");
        materialRequest.setUrl("http://url.com");
        request.setMaterial(materialRequest);

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@lol.com")
                .role(Role.TEACHER)
                .build();

        when(meService.getMe()).thenReturn(teacher);

        assertThrows(BadRequestException.class, () -> postService.createPost(request));
        verify(postRepository, never()).save(any(PostEntity.class));
    }
}
