package com.example.PotteryPotSchool.Comments;


import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Comments.CommentCreateRequest;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateCommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private CommentsService commentService;

    @Test
    void shouldCreateCommentForAuthorizedUser() {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody("У меня есть несколько вопросов по заданию");

        User user = User.builder().id(authorId).email("student@example.com").role(Role.STUDENT).build();

        PostEntity post = PostEntity.builder().id(postId).title("Пост").description("Описание").build();

        when(meService.getMe()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        LocalDateTime now = LocalDateTime.now();

        when(commentRepository.save(any(CommentEntity.class))).thenAnswer(invocation -> {
            CommentEntity comment = invocation.getArgument(0);
            comment.setId(commentId);
            comment.setCreatedAt(now);
            return comment;
        });

        CommentDetails result = commentService.create(postId, request);

        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals(postId, result.getPostId());
        assertEquals(authorId, result.getAuthorId());
        assertEquals("У меня есть несколько вопросов по заданию", result.getBody());
        assertEquals(now, result.getCreatedAt());

        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(captor.capture());

        CommentEntity savedComment = captor.getValue();
        assertEquals(postId, savedComment.getPostId());
        assertEquals(authorId, savedComment.getAuthorId());
        assertEquals("У меня есть несколько вопросов по заданию", savedComment.getBody());
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {
        UUID postId = UUID.randomUUID();

        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody("Комментарий");

        when(meService.getMe()).thenThrow(new UnauthorizedException("Unauthorized"));

        assertThrows(UnauthorizedException.class, () -> commentService.create(postId, request));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();

        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody("Комментарий");

        User user = User.builder().id(UUID.randomUUID()).email("student@example.com").role(Role.STUDENT).build();

        when(meService.getMe()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.create(postId, request));

        verify(meService).getMe();
        verify(postRepository).findById(postId);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenBodyIsNull() {
        UUID postId = UUID.randomUUID();

        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody(null);

        User user = User.builder().id(UUID.randomUUID()).email("student@example.com").role(Role.STUDENT).build();

        when(meService.getMe()).thenReturn(user);

        assertThrows(BadRequestException.class, () -> commentService.create(postId, request));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenBodyIsBlank() {
        UUID postId = UUID.randomUUID();

        CommentCreateRequest request = new CommentCreateRequest();
        request.setBody("   ");

        User user = User.builder().id(UUID.randomUUID()).email("student@example.com").role(Role.STUDENT).build();

        when(meService.getMe()).thenReturn(user);

        assertThrows(BadRequestException.class, () -> commentService.create(postId, request));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
        verifyNoInteractions(commentRepository);
    }
}
