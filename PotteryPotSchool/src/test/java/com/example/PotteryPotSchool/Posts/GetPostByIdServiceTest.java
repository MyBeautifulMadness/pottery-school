package com.example.PotteryPotSchool.Posts;

import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Posts.PostDetails;
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
import org.springframework.data.crossstore.ChangeSetPersister;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetPostByIdServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private PostServiceImpl getPostByIdService;

    @Test
    void shouldReturnMaterialPostByIdForAuthorizedUser() {

        UUID postId = UUID.randomUUID();

        User student = User.builder().id(UUID.randomUUID()).email("student@gmail.com").role(Role.STUDENT).build();

        when(meService.getMe()).thenReturn(student);

        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 8, 13, 11, 57);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 8, 13, 11, 57);

        PostEntity post = PostEntity.builder()
                .id(postId)
                .type(PostType.MATERIAL)
                .title("Основы проекта")
                .description("Важный материал")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        MaterialEntity material = MaterialEntity.builder()
                .id(UUID.randomUUID())
                .type(MaterialType.LINK)
                .title("Ссылка")
                .url("http://link.com/link")
                .text("Дополнительное описание")
                .post(post)
                .build();

        post.setMaterial(material);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostDetails result = getPostByIdService.getById(postId);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals(PostType.MATERIAL, result.getType());
        assertEquals("Основы проекта", result.getTitle());
        assertEquals("Важный материал", result.getDescription());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());

        assertNotNull(result.getMaterial());
        assertEquals(MaterialType.LINK, result.getMaterial().getType());
        assertEquals("Ссылка", result.getMaterial().getTitle());
        assertEquals("http://link.com/link", result.getMaterial().getUrl());
        assertEquals("Дополнительное описание", result.getMaterial().getText());

        assertNull(result.getTask());

        verify(meService).getMe();
        verify(postRepository).findById(postId);
    }

    @Test
    void shouldReturnTaskPostByIdForAuthorizedUser() {

        UUID postId = UUID.randomUUID();

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@gmail.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);

        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 8, 13, 11, 57);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 8, 13, 11, 57);
        LocalDateTime deadline = LocalDateTime.of(2026, 3, 20, 18, 0);

        PostEntity post = PostEntity.builder()
                .id(postId)
                .type(PostType.TASK)
                .title("Сделать горшок")
                .description("Практическое задание")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        TaskEntity task = TaskEntity.builder()
                .id(UUID.randomUUID())
                .description("Слепить горшок")
                .deadline(deadline)
                .post(post)
                .build();

        post.setTask(task);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostDetails result = getPostByIdService.getById(postId);
        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals(PostType.TASK, result.getType());
        assertEquals("Сделать горшок", result.getTitle());
        assertEquals("Практическое задание", result.getDescription());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());

        assertNull(result.getMaterial());
        assertNotNull(result.getTask());
        assertEquals("Слепить горшок", result.getTask().getDescription());
        assertEquals(deadline, result.getTask().getDeadline());

        verify(meService).getMe();
        verify(postRepository).findById(postId);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {

        UUID postId = UUID.randomUUID();

        when(meService.getMe()).thenThrow(new UnauthorizedException("Unauthorized"));
        assertThrows(UnauthorizedException.class, () -> getPostByIdService.getById(postId));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPostDoesNotExist() {

        UUID postId = UUID.randomUUID();

        User student = User.builder()
                .id(UUID.randomUUID())
                .email("student@example.com")
                .role(Role.STUDENT)
                .build();

        when(meService.getMe()).thenReturn(student);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> getPostByIdService.getById(postId));

        verify(meService).getMe();
        verify(postRepository).findById(postId);
    }
}
