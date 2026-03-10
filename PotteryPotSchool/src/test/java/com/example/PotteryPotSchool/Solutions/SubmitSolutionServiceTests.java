package com.example.PotteryPotSchool.Solutions;

import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Solutions.Solution;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Solutions.impl.SolutionServiceImpl;
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
public class SubmitSolutionServiceTests {

    @Mock
    private SolutionRepository solutionRepository;

    @Mock
    private MeService meService;

    @InjectMocks
    private SolutionServiceImpl solutionService;

    @Test
    void shouldSubmitOwnDraftSolution() {
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
                .id(UUID.randomUUID())
                .type(PostType.TASK)
                .build();

        SolutionEntity draft = SolutionEntity.builder()
                .id(solutionId)
                .post(taskPost)
                .studentId(studentId)
                .status(SolutionStatus.DRAFT)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(meService.getMe()).thenReturn(currentUser);
        when(solutionRepository.findById(solutionId)).thenReturn(Optional.of(draft));
        when(solutionRepository.save(any(SolutionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Solution result = solutionService.submit(solutionId);

        assertEquals(SolutionStatus.SUBMITTED, result.getStatus());
        assertNotNull(result.getSubmittedAt());
        verify(solutionRepository).save(any(SolutionEntity.class));
    }

    @Test
    void shouldThrowNotFoundWhenSolutionDoesNotExist() {
        UUID studentId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(studentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        when(meService.getMe()).thenReturn(currentUser);
        when(solutionRepository.findById(solutionId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> solutionService.submit(solutionId));
        verify(solutionRepository, never()).save(any());
    }

    @Test
    void shouldThrowForbiddenWhenSubmittingSomeoneElsesSolution() {
        UUID currentStudentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID solutionId = UUID.randomUUID();

        User currentUser = User.builder()
                .id(currentStudentId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .email("owner@test.com")
                .role(Role.STUDENT)
                .build();

        PostEntity taskPost = PostEntity.builder()
                .id(UUID.randomUUID())
                .type(PostType.TASK)
                .build();

        SolutionEntity solution = SolutionEntity.builder()
                .id(solutionId)
                .post(taskPost)
                .studentId(owner.getId())
                .status(SolutionStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(meService.getMe()).thenReturn(currentUser);
        when(solutionRepository.findById(solutionId)).thenReturn(Optional.of(solution));

        assertThrows(ForbiddenException.class, () -> solutionService.submit(solutionId));
        verify(solutionRepository, never()).save(any());
    }

    @Test
    void shouldThrowForbiddenWhenCurrentUserIsTeacher() {
        UUID solutionId = UUID.randomUUID();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@test.com")
                .role(Role.TEACHER)
                .build();

        when(meService.getMe()).thenReturn(teacher);

        assertThrows(ForbiddenException.class, () -> solutionService.submit(solutionId));
        verify(solutionRepository, never()).findById(any());
    }
}
