package com.example.PotteryPotSchool.Comments;


import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Comments.CommentDetails;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Comments.CommentEntity;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.CommentRepository;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.service.Comments.CommentsService;
import com.example.PotteryPotSchool.service.Comments.impl.CommentServiceImpl;
import com.example.PotteryPotSchool.service.Me.MeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetCommentsByPostIdServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void shouldReturnAllCommentsForPostForAuthorizedUser() {
        UUID postId = UUID.randomUUID();

        User student = User.builder().id(UUID.randomUUID()).email("student@example.com").role(Role.STUDENT).build();

        PostEntity post = PostEntity.builder().id(postId).title("Пост").description("Описание").build();

        CommentEntity comment1 = CommentEntity.builder()
                .id(UUID.randomUUID())
                .postId(postId)
                .authorId(UUID.randomUUID())
                .body("Первый комментарий")
                .createdAt(LocalDateTime.of(2026, 3, 10, 10, 0))
                .build();

        CommentEntity comment2 = CommentEntity.builder()
                .id(UUID.randomUUID())
                .postId(postId)
                .authorId(UUID.randomUUID())
                .body("Второй комментарий")
                .createdAt(LocalDateTime.of(2026, 3, 10, 11, 0))
                .build();

        when(meService.getMe()).thenReturn(student);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findAllByPostId(postId)).thenReturn(List.of(comment1, comment2));

        List<CommentDetails> result = commentService.getByPostId(postId);

        assertNotNull(result);
        assertEquals(2, result.size());

        CommentDetails first = result.get(0);
        assertEquals(comment1.getId(), first.getId());
        assertEquals(postId, first.getPostId());
        assertEquals(comment1.getAuthorId(), first.getAuthorId());
        assertEquals("Первый комментарий", first.getBody());
        assertEquals(comment1.getCreatedAt(), first.getCreatedAt());

        CommentDetails second = result.get(1);
        assertEquals(comment2.getId(), second.getId());
        assertEquals(postId, second.getPostId());
        assertEquals(comment2.getAuthorId(), second.getAuthorId());
        assertEquals("Второй комментарий", second.getBody());
        assertEquals(comment2.getCreatedAt(), second.getCreatedAt());

        verify(meService).getMe();
        verify(postRepository).findById(postId);
        verify(commentRepository).findAllByPostId(postId);
    }

    @Test
    void shouldReturnEmptyListWhenPostHasNoComments() {
        UUID postId = UUID.randomUUID();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@example.com")
                .role(Role.TEACHER)
                .build();

        PostEntity post = PostEntity.builder()
                .id(postId)
                .title("Пост без комментариев")
                .description("Описание")
                .build();

        when(meService.getMe()).thenReturn(teacher);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findAllByPostId(postId)).thenReturn(List.of());

        List<CommentDetails> result = commentService.getByPostId(postId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(meService).getMe();
        verify(postRepository).findById(postId);
        verify(commentRepository).findAllByPostId(postId);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {
        UUID postId = UUID.randomUUID();

        when(meService.getMe()).thenThrow(new UnauthorizedException("Unauthorized"));

        assertThrows(UnauthorizedException.class, () -> commentService.getByPostId(postId));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
        verifyNoInteractions(commentRepository);
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

        assertThrows(NotFoundException.class, () -> commentService.getByPostId(postId));

        verify(meService).getMe();
        verify(postRepository).findById(postId);
        verifyNoInteractions(commentRepository);
    }
}
