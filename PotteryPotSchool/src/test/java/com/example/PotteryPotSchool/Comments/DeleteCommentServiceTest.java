package com.example.PotteryPotSchool.Comments;

import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Comments.CommentEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.CommentRepository;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.service.Comments.impl.CommentServiceImpl;
import com.example.PotteryPotSchool.service.Me.MeService;
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
public class DeleteCommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void shouldDeleteCommentWhenUserIsAuthor() {
        UUID authorId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        User author = User.builder().id(authorId).email("author@example.com").role(Role.STUDENT).build();

        CommentEntity comment = CommentEntity.builder()
                .id(commentId)
                .postId(UUID.randomUUID())
                .authorId(authorId)
                .body("Мой комментарий")
                .createdAt(LocalDateTime.of(2026, 3, 10, 12, 0))
                .build();

        when(meService.getMe()).thenReturn(author);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.delete(commentId);

        verify(meService).getMe();
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    void shouldDeleteCommentWhenUserIsTeacher() {

        UUID commentId = UUID.randomUUID();

        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@example.com").role(Role.TEACHER).build();

        CommentEntity comment = CommentEntity.builder()
                .id(commentId)
                .postId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .body("Чужой комментарий")
                .createdAt(LocalDateTime.of(2026, 3, 10, 12, 0))
                .build();

        when(meService.getMe()).thenReturn(teacher);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.delete(commentId);

        verify(meService).getMe();
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {
        UUID commentId = UUID.randomUUID();

        when(meService.getMe()).thenThrow(new UnauthorizedException("Unauthorized"));

        assertThrows(UnauthorizedException.class, () -> commentService.delete(commentId));

        verify(meService).getMe();
        verifyNoInteractions(commentRepository);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCommentDoesNotExist() {
        UUID commentId = UUID.randomUUID();

        User user = User.builder().id(UUID.randomUUID()).email("student@example.com").role(Role.STUDENT).build();

        when(meService.getMe()).thenReturn(user);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.delete(commentId));

        verify(meService).getMe();
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any(CommentEntity.class));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUserIsNotAuthorAndNotTeacher() {
        UUID commentId = UUID.randomUUID();

        User anotherStudent = User.builder().id(UUID.randomUUID()).email("another@example.com").role(Role.STUDENT).build();

        CommentEntity comment = CommentEntity.builder()
                .id(commentId)
                .postId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .body("Чужой комментарий")
                .createdAt(LocalDateTime.of(2026, 3, 10, 12, 0))
                .build();

        when(meService.getMe()).thenReturn(anotherStudent);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(ForbiddenException.class, () -> commentService.delete(commentId));

        verify(meService).getMe();
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any(CommentEntity.class));
    }
}
