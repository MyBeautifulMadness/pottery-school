package com.example.PotteryPotSchool.Posts;

import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Posts.Paged;
import com.example.PotteryPotSchool.dto.Posts.PostShortDetails;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetPostsServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private PostServiceImpl getPostsService;

    @Test
    void shouldReturnPagedPostsWithoutFilterForAuthorizedUser() {

        User student = User.builder().id(UUID.randomUUID()).email("student@lol4k.com").role(Role.STUDENT).build();

        when(meService.getMe()).thenReturn(student);

        PostEntity post1 = PostEntity.builder()
                .id(UUID.randomUUID())
                .type(PostType.MATERIAL)
                .title("Материал 1")
                .description("Описание 1")
                .createdAt(LocalDateTime.of(2026, 3, 8, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 8, 12, 10))
                .build();

        PostEntity post2 = PostEntity.builder()
                .id(UUID.randomUUID())
                .type(PostType.TASK)
                .title("Задание 1")
                .description("Описание 2")
                .createdAt(LocalDateTime.of(2026, 3, 8, 13, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 8, 13, 10))
                .build();

        Pageable pageable = PageRequest.of(0, 2);
        Page<PostEntity> postPage = new PageImpl<>(List.of(post1, post2), pageable, 2);

        when(postRepository.findAll(pageable)).thenReturn(postPage);

        Paged<PostShortDetails> result = getPostsService.getPosts(null, 0, 2);

        assertNotNull(result);
        assertEquals(0, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(2, result.getTotal());

        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());

        PostShortDetails first = result.getItems().get(0);
        assertEquals(post1.getId(), first.getId());
        assertEquals(PostType.MATERIAL, first.getType());
        assertEquals("Материал 1", first.getTitle());
        assertEquals("Описание 1", first.getDescription());
        assertEquals(post1.getCreatedAt(), first.getCreatedAt());
        assertEquals(post1.getUpdatedAt(), first.getUpdatedAt());

        PostShortDetails second = result.getItems().get(1);
        assertEquals(post2.getId(), second.getId());
        assertEquals(PostType.TASK, second.getType());
        assertEquals("Задание 1", second.getTitle());
        assertEquals("Описание 2", second.getDescription());
        assertEquals(post2.getCreatedAt(), second.getCreatedAt());
        assertEquals(post2.getUpdatedAt(), second.getUpdatedAt());

        verify(meService).getMe();
        verify(postRepository).findAll(pageable);
        verify(postRepository, never()).findAllByType(any(), any());
    }

    @Test
    void shouldReturnPagedPostsFilteredByTypeForAuthorizedUser() {
        User teacher = User.builder().id(UUID.randomUUID()).email("teacher@lol4k.com").role(Role.TEACHER).build();

        when(meService.getMe()).thenReturn(teacher);

        PostEntity post = PostEntity.builder()
                .id(UUID.randomUUID())
                .type(PostType.MATERIAL)
                .title("Новая техника")
                .description("Новый материал")
                .createdAt(LocalDateTime.of(2026, 3, 8, 14, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 8, 14, 5))
                .build();

        Pageable pageable = PageRequest.of(1, 3);
        Page<PostEntity> postPage = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findAllByType(PostType.MATERIAL, pageable)).thenReturn(postPage);

        Paged<PostShortDetails> result = getPostsService.getPosts(PostType.MATERIAL, 1, 3);

        assertNotNull(result);
        assertEquals(1, result.getPage());
        assertEquals(3, result.getSize());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());

        PostShortDetails item = result.getItems().get(0);
        assertEquals(post.getId(), item.getId());
        assertEquals(PostType.MATERIAL, item.getType());
        assertEquals("Новая техника", item.getTitle());
        assertEquals("Новый материал", item.getDescription());
        assertEquals(post.getCreatedAt(), item.getCreatedAt());
        assertEquals(post.getUpdatedAt(), item.getUpdatedAt());

        verify(meService).getMe();
        verify(postRepository).findAllByType(PostType.MATERIAL, pageable);
        verify(postRepository, never()).findAll(pageable);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthenticated() {

        when(meService.getMe()).thenThrow(new UnauthorizedException("Unauthorized"));

        assertThrows(UnauthorizedException.class, () -> getPostsService.getPosts(null, 0, 10));

        verify(meService).getMe();
        verifyNoInteractions(postRepository);
    }
}
