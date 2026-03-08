package com.example.PotteryPotSchool.Posts;

import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Post.PostService;
import com.example.PotteryPotSchool.service.Post.impl.PostServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class DeletePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private PostServiceImpl deletePostService;

    @Test
    void shouldDeletePostWhenUserIsTeacher() {
        UUID postId = UUID.randomUUID();
        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@lol.com").role(Role.TEACHER).build();

        PostEntity post = PostEntity.builder()
                .id(postId)
                .title("Пост для удаления")
                .description("Описание")
                .createdAt(LocalDateTime.of(2026, 3, 8, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 8, 12, 0))
                .build();

        when(meService.getMe()).thenReturn(teacher);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        deletePostService.delete(postId);

        verify(meService).getMe();
        verify(postRepository).findById(postId);
        verify(postRepository).delete(post);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {
        UUID postId = UUID.randomUUID();

        when(meService.getMe()).thenThrow(new UnauthorizedException("Unauthorized"));

        assertThrows(UnauthorizedException.class, () -> deletePostService.delete(postId));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUserIsNotTeacher() {
        UUID postId = UUID.randomUUID();

        User student = User.builder().id(UUID.randomUUID()).email("student@example.com").role(Role.STUDENT).build();

        when(meService.getMe()).thenReturn(student);

        assertThrows(ForbiddenException.class, () -> deletePostService.delete(postId));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@example.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> deletePostService.delete(postId));

        verify(meService).getMe();
        verify(postRepository).findById(postId);
        verify(postRepository, never()).delete(any(PostEntity.class));
    }
}
