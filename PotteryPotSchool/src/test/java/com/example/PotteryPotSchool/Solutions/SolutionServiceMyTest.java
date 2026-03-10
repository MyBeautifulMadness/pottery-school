package com.example.PotteryPotSchool.Solutions;

import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Solutions.SolutionDto;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.service.Solutions.SolutionMapper;
import com.example.PotteryPotSchool.service.Solutions.impl.SolutionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SolutionServiceMyTest {

    @Mock
    private SolutionRepository solutionRepository;

    @Mock
    private SolutionMapper solutionMapper;

    @InjectMocks
    private SolutionServiceImpl solutionService;

    private UUID postId;
    private UUID studentId;
    private SolutionEntity solutionEntity;
    private SolutionDto solutionDto;

    @BeforeEach
    void setup() {
        postId = UUID.randomUUID();
        studentId = UUID.randomUUID();

        PostEntity post = PostEntity.builder().id(postId).build();
        solutionEntity = SolutionEntity.builder()
                .id(UUID.randomUUID())
                .post(post)
                .studentId(studentId)
                .status(SolutionStatus.DRAFT)
                .text("Test text")
                .videoUrl("http://video.url")
                .attachmentUrl("http://file.url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .submittedAt(null)
                .build();

        solutionDto = SolutionDto.builder()
                .id(solutionEntity.getId())
                .postId(postId)
                .studentId(studentId)
                .status(SolutionStatus.DRAFT)
                .text("Test text")
                .videoUrl("http://video.url")
                .attachmentUrl("http://file.url")
                .createdAt(solutionEntity.getCreatedAt())
                .updatedAt(solutionEntity.getUpdatedAt())
                .submittedAt(null)
                .build();
    }

    @Test
    void getMySolution_success_returnsDto() {
        Mockito.when(solutionRepository.findByPostIdAndStudentId(postId, studentId))
                .thenReturn(Optional.of(solutionEntity));
        Mockito.when(solutionMapper.toDto(solutionEntity))
                .thenReturn(solutionDto);

        SolutionDto result = solutionService.getMySolution(postId, studentId);

        assertNotNull(result);
        assertEquals(solutionDto.getId(), result.getId());
        assertEquals(solutionDto.getPostId(), result.getPostId());
        assertEquals(solutionDto.getStudentId(), result.getStudentId());
        assertEquals(solutionDto.getStatus(), result.getStatus());
        assertEquals(solutionDto.getText(), result.getText());
    }

    @Test
    void getMySolution_notFound_throwsNotFound() {
        Mockito.when(solutionRepository.findByPostIdAndStudentId(postId, studentId))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> solutionService.getMySolution(postId, studentId));

        assertEquals("Solution not found", ex.getMessage());
    }

    @Test
    void getMySolution_unauthorized_throwsUnauthorized() {
        UUID invalidStudentId = UUID.randomUUID();

        Mockito.when(solutionRepository.findByPostIdAndStudentId(postId, invalidStudentId))
                .thenThrow(new UnauthorizedException("Unauthorized"));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> solutionService.getMySolution(postId, invalidStudentId));

        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void getMySolution_withSubmittedAt_notNull() {
        solutionEntity.setSubmittedAt(LocalDateTime.now());
        solutionDto.setSubmittedAt(solutionEntity.getSubmittedAt());

        Mockito.when(solutionRepository.findByPostIdAndStudentId(postId, studentId))
                .thenReturn(Optional.of(solutionEntity));
        Mockito.when(solutionMapper.toDto(solutionEntity))
                .thenReturn(solutionDto);

        SolutionDto result = solutionService.getMySolution(postId, studentId);

        assertNotNull(result.getSubmittedAt());
        assertEquals(solutionDto.getSubmittedAt(), result.getSubmittedAt());
    }
}
