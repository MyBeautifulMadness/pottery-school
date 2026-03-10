package com.example.PotteryPotSchool.Solutions;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateOrUpdateSolutionServiceTests {

    @Mock
    private SolutionRepository solutionRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private SolutionServiceImpl solutionService;

    @Test
    void shouldCreateDraftSolutionWhenStudentSavesTaskPost() {
        UUID postId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(studentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        UserEntity studentEntity = UserEntity.builder()
                .id(studentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        PostEntity taskPost = PostEntity.builder()
                .id(postId)
                .type(PostType.TASK)
                .title("Task")
                .build();

        SolutionUpsertRequest request = new SolutionUpsertRequest();
        request.setText("answer");
        request.setVideoUrl("video");
        request.setAttachmentUrl("file");
        request.setSubmit(false);

        when(meService.getMe()).thenReturn(currentUser);
        when(postRepository.findById(postId)).thenReturn(Optional.of(taskPost));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(studentEntity));
        when(solutionRepository.findByPostIdAndStudentId(postId, studentId)).thenReturn(Optional.empty());
        when(solutionRepository.save(any(SolutionEntity.class))).thenAnswer(invocation -> {
            SolutionEntity solution = invocation.getArgument(0);
            solution.setId(solutionId);
            return solution;
        });

        Solution result = solutionService.createOrUpdate(postId, request);

        assertEquals(solutionId, result.getId());
        assertEquals(postId, result.getPostId());
        assertEquals(studentId, result.getStudentId());
        assertEquals(SolutionStatus.DRAFT, result.getStatus());
        assertEquals("answer", result.getText());
        assertEquals("video", result.getVideoUrl());
        assertEquals("file", result.getAttachmentUrl());
        assertNull(result.getSubmittedAt());

        verify(solutionRepository).save(any(SolutionEntity.class));
    }

    @Test
    void shouldCreateSubmittedSolutionWhenSubmitFlagIsTrue() {
        UUID postId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(studentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        UserEntity studentEntity = UserEntity.builder()
                .id(studentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        PostEntity taskPost = PostEntity.builder()
                .id(postId)
                .type(PostType.TASK)
                .title("Task")
                .build();

        SolutionUpsertRequest request = new SolutionUpsertRequest();
        request.setText("final answer");
        request.setSubmit(true);

        when(meService.getMe()).thenReturn(currentUser);
        when(postRepository.findById(postId)).thenReturn(Optional.of(taskPost));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(studentEntity));
        when(solutionRepository.findByPostIdAndStudentId(postId, studentId)).thenReturn(Optional.empty());
        when(solutionRepository.save(any(SolutionEntity.class))).thenAnswer(invocation -> {
            SolutionEntity solution = invocation.getArgument(0);
            solution.setId(solutionId);
            return solution;
        });

        Solution result = solutionService.createOrUpdate(postId, request);

        assertEquals(SolutionStatus.SUBMITTED, result.getStatus());
        assertNotNull(result.getSubmittedAt());
        assertEquals("final answer", result.getText());
    }

    @Test
    void shouldUpdateExistingStudentSolution() {
        UUID postId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        User currentUser = User.builder()
                .id(studentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        UserEntity studentEntity = UserEntity.builder()
                .id(studentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        PostEntity taskPost = PostEntity.builder()
                .id(postId)
                .type(PostType.TASK)
                .title("Task")
                .build();

        SolutionEntity existing = SolutionEntity.builder()
                .id(solutionId)
                .post(taskPost)
                .student(studentId)
                .status(SolutionStatus.DRAFT)
                .text("old")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        SolutionUpsertRequest request = new SolutionUpsertRequest();
        request.setText("new text");
        request.setSubmit(false);

        when(meService.getMe()).thenReturn(currentUser);
        when(postRepository.findById(postId)).thenReturn(Optional.of(taskPost));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(studentEntity));
        when(solutionRepository.findByPostIdAndStudentId(postId, studentId)).thenReturn(Optional.of(existing));
        when(solutionRepository.save(any(SolutionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Solution result = solutionService.createOrUpdate(postId, request);

        assertEquals(solutionId, result.getId());
        assertEquals("new text", result.getText());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(SolutionStatus.DRAFT, result.getStatus());
    }

    @Test
    void shouldThrowForbiddenWhenCurrentUserIsTeacher() {
        UUID postId = UUID.randomUUID();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@test.com")
                .role(Role.TEACHER)
                .build();

        SolutionUpsertRequest request = new SolutionUpsertRequest();
        request.setText("answer");

        when(meService.getMe()).thenReturn(teacher);

        assertThrows(ForbiddenException.class, () -> solutionService.createOrUpdate(postId, request));

        verify(postRepository, never()).findById(any());
        verify(solutionRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(studentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        SolutionUpsertRequest request = new SolutionUpsertRequest();
        request.setText("answer");

        when(meService.getMe()).thenReturn(currentUser);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> solutionService.createOrUpdate(postId, request));

        verify(solutionRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenPostIsNotTask() {
        UUID postId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(studentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        PostEntity materialPost = PostEntity.builder()
                .id(postId)
                .type(PostType.MATERIAL)
                .title("Material")
                .build();

        SolutionUpsertRequest request = new SolutionUpsertRequest();
        request.setText("answer");

        when(meService.getMe()).thenReturn(currentUser);
        when(postRepository.findById(postId)).thenReturn(Optional.of(materialPost));

        assertThrows(BadRequestException.class, () -> solutionService.createOrUpdate(postId, request));

        verify(solutionRepository, never()).save(any());
    }
}
