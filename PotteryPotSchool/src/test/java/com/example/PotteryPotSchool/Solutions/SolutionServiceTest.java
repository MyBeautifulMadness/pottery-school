package com.example.PotteryPotSchool.Solutions;

import com.example.PotteryPotSchool.dto.Solutions.SolutionSummaryDto;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.service.Solutions.impl.SolutionServiceImpl;
import com.example.PotteryPotSchool.security.UserPrincipal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SolutionServiceTest {

    @Mock
    private SolutionRepository solutionRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private SolutionServiceImpl solutionService;

    private UUID postId;

    @BeforeEach
    void setup() {
        postId = UUID.randomUUID();
    }

    @Test
    void getSolutions_success_returnsList() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        PostEntity post = PostEntity.builder()
                .id(postId)
                .type(PostType.TASK)
                .build();

        Mockito.when(postRepository.findById(postId))
                .thenReturn(Optional.of(post));

        SolutionSummaryDto s1 =
                SolutionSummaryDto.builder()
                        .id(UUID.randomUUID())
                        .postId(postId)
                        .studentId(UUID.randomUUID())
                        .status(SolutionStatus.DRAFT)
                        .submittedAt(null)
                        .build();

        SolutionSummaryDto s2 =
                SolutionSummaryDto.builder()
                        .id(UUID.randomUUID())
                        .postId(postId)
                        .studentId(UUID.randomUUID())
                        .status(SolutionStatus.SUBMITTED)
                        .submittedAt(LocalDateTime.now())
                        .build();

        Mockito.when(solutionRepository.findByPostId(postId))
                .thenReturn(List.of(s1, s2));

        List<SolutionSummaryDto> result =
                solutionService.getSolutions(postId, null, teacher);

        assertEquals(2, result.size());
    }

    @Test
    void getSolutions_statusFilter_returnsOnlySubmitted() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        PostEntity post = PostEntity.builder()
                .id(postId)
                .type(PostType.TASK)
                .build();

        Mockito.when(postRepository.findById(postId))
                .thenReturn(Optional.of(post));

        SolutionSummaryDto submitted =
                SolutionSummaryDto.builder()
                        .id(UUID.randomUUID())
                        .postId(postId)
                        .studentId(UUID.randomUUID())
                        .status(SolutionStatus.SUBMITTED)
                        .submittedAt(LocalDateTime.now())
                        .build();

        Mockito.when(solutionRepository
                        .findByPostIdAndStatus(postId, SolutionStatus.SUBMITTED))
                .thenReturn(List.of(submitted));

        List<SolutionSummaryDto> result =
                solutionService.getSolutions(postId, SolutionStatus.SUBMITTED, teacher);

        assertEquals(1, result.size());
        assertEquals(SolutionStatus.SUBMITTED, result.get(0).getStatus());
    }

    @Test
    void getSolutions_postNotFound_throwsNotFound() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        Mockito.when(postRepository.findById(postId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> solutionService.getSolutions(postId, null, teacher)
        );
    }

    @Test
    void getSolutions_notTeacher_throwsForbidden() {

        UserPrincipal student =
                new UserPrincipal(UUID.randomUUID(), "student@mail.com", Role.STUDENT);

        assertThrows(
                ForbiddenException.class,
                () -> solutionService.getSolutions(postId, null, student)
        );
    }

}
