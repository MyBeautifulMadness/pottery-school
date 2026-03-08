package com.example.PotteryPotSchool.Posts;


import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Posts.MaterialUpdateRequest;
import com.example.PotteryPotSchool.dto.Posts.PostDetails;
import com.example.PotteryPotSchool.dto.Posts.PostUpdateRequest;
import com.example.PotteryPotSchool.dto.Posts.TaskUpdateRequest;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.MaterialEntity;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Posts.MaterialType;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Post.impl.PostServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdatePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private PostServiceImpl updatePostService;


    @Test
    void shouldUpdateMaterialPostWhenUserIsTeacher() {

        UUID postId = UUID.randomUUID();

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@lol.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);
        PostEntity post = PostEntity.builder()
                .id(postId)
                .type(PostType.MATERIAL)
                .title("Старый заголовок")
                .description("Старое описание")
                .createdAt(LocalDateTime.of(2026, 3, 8, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 8, 10, 0))
                .build();

        MaterialEntity material = MaterialEntity.builder()
                .id(UUID.randomUUID())
                .type(MaterialType.LINK)
                .title("Старый material заголовок")
                .url("https://oldlink.com")
                .text("Старый текст")
                .post(post)
                .build();

        post.setMaterial(material);

        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle("Новый заголовок");
        request.setDescription("Новое описание");

        MaterialUpdateRequest materialRequest = new MaterialUpdateRequest();
        materialRequest.setTitle("Новый material заголовок");
        materialRequest.setUrl("https://newlink.com");
        materialRequest.setText("Новый текст");
        request.setMaterial(materialRequest);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity saved = invocation.getArgument(0);
            saved.setUpdatedAt(LocalDateTime.of(2026, 3, 8, 13, 27, 3));
            return saved;
        });

        PostDetails result = updatePostService.update(postId, request);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals(PostType.MATERIAL, result.getType());
        assertEquals("Новый заголовок", result.getTitle());
        assertEquals("Новое описание", result.getDescription());
        assertNotNull(result.getMaterial());
        assertEquals(MaterialType.LINK, result.getMaterial().getType());
        assertEquals("Новый material заголовок", result.getMaterial().getTitle());
        assertEquals("https://newlink.com", result.getMaterial().getUrl());
        assertEquals("Новый текст", result.getMaterial().getText());
        assertNull(result.getTask());

        verify(postRepository).findById(postId);
        verify(postRepository).save(post);
    }


    @Test
    void shouldUpdateTaskPostWhenUserIsTeacher() {
        UUID postId = UUID.randomUUID();

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@lol.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);

        PostEntity post = PostEntity.builder()
                .id(postId)
                .type(PostType.TASK)
                .title("Старое задание")
                .description("Старое описание")
                .createdAt(LocalDateTime.of(2026, 3, 8, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 8, 10, 0))
                .build();

        TaskEntity task = TaskEntity.builder()
                .id(UUID.randomUUID())
                .description("Старое условие")
                .deadline(LocalDateTime.of(2026, 3, 20, 18, 0))
                .post(post)
                .build();

        post.setTask(task);

        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle("Новое задание");
        request.setDescription("Новое описание");

        TaskUpdateRequest taskRequest = new TaskUpdateRequest();
        taskRequest.setDescription("Новое условие");
        taskRequest.setDeadline(LocalDateTime.of(2026, 3, 25, 20, 0));
        request.setTask(taskRequest);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity saved = invocation.getArgument(0);
            saved.setUpdatedAt(LocalDateTime.of(2026, 3, 8, 13, 27, 3));
            return saved;
        });

        PostDetails result = updatePostService.update(postId, request);
        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals(PostType.TASK, result.getType());
        assertEquals("Новое задание", result.getTitle());
        assertEquals("Новое описание", result.getDescription());
        assertNull(result.getMaterial());
        assertNotNull(result.getTask());
        assertEquals("Новое условие", result.getTask().getDescription());
        assertEquals(LocalDateTime.of(2026, 3, 25, 20, 0), result.getTask().getDeadline());

        verify(postRepository).findById(postId);
        verify(postRepository).save(post);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {
        UUID postId = UUID.randomUUID();
        PostUpdateRequest request = new PostUpdateRequest();

        when(meService.getMe()).thenThrow(new UnauthorizedException("Unauthorized"));

        assertThrows(UnauthorizedException.class, () -> updatePostService.update(postId, request));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUserIsNotTeacher() {
        UUID postId = UUID.randomUUID();
        PostUpdateRequest request = new PostUpdateRequest();

        User student = User.builder().id(UUID.randomUUID()).email("student@lol.com").role(Role.STUDENT).build();

        when(meService.getMe()).thenReturn(student);
        assertThrows(ForbiddenException.class, () -> updatePostService.update(postId, request));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        PostUpdateRequest request = new PostUpdateRequest();

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@lol.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> updatePostService.update(postId, request));

        verify(meService).getMe();
        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenTryingToUpdateTaskInsideMaterialPost() {
        UUID postId = UUID.randomUUID();

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@lol.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);

        PostEntity materialPost = PostEntity.builder()
                .id(postId)
                .type(PostType.MATERIAL)
                .title("Материал")
                .description("Описание")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        materialPost.setMaterial(MaterialEntity.builder()
                .id(UUID.randomUUID())
                .type(MaterialType.TEXT)
                .title("Материал")
                .text("Текст")
                .post(materialPost)
                .build());

        PostUpdateRequest request = new PostUpdateRequest();
        TaskUpdateRequest taskRequest = new TaskUpdateRequest();
        taskRequest.setDescription("Нельзя обновлять task у MATERIAL");
        request.setTask(taskRequest);

        when(postRepository.findById(postId)).thenReturn(Optional.of(materialPost));

        assertThrows(BadRequestException.class, () -> updatePostService.update(postId, request));

        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenTryingToUpdateMaterialInsideTaskPost() {
        UUID postId = UUID.randomUUID();

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@lol.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);
        PostEntity taskPost = PostEntity.builder()
                .id(postId)
                .type(PostType.TASK)
                .title("Задание")
                .description("Описание")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskPost.setTask(TaskEntity.builder()
                .id(UUID.randomUUID())
                .description("Старое условие")
                .deadline(LocalDateTime.of(2026, 3, 20, 18, 0))
                .post(taskPost)
                .build());

        PostUpdateRequest request = new PostUpdateRequest();
        MaterialUpdateRequest materialRequest = new MaterialUpdateRequest();
        materialRequest.setTitle("Нельзя обновлять material у TASK");
        materialRequest.setUrl("https://example.com");
        request.setMaterial(materialRequest);

        when(postRepository.findById(postId)).thenReturn(Optional.of(taskPost));

        assertThrows(BadRequestException.class, () -> updatePostService.update(postId, request));

        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any());
    }
}
