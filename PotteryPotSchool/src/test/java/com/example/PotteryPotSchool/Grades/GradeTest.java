package com.example.PotteryPotSchool.Grades;

import com.example.PotteryPotSchool.config.*;
import com.example.PotteryPotSchool.dto.Grades.*;
import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.GradeRepository;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Grades.impl.GradeServiceImpl;
import com.example.PotteryPotSchool.service.Login.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private SolutionRepository solutionRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private String token;
    private UUID solutionId;
    private UUID teacherId;

    @BeforeEach
    void setup() {

        MockitoAnnotations.openMocks(this);

        token = "valid-token";
        solutionId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
    }

    @Test
    void createGrade_success() {

        GradeUpsertRequest request = new GradeUpsertRequest(5, "Good");

        SolutionEntity solution = new SolutionEntity();
        solution.setId(solutionId);

        UserPrincipal teacher = new UserPrincipal(teacherId, "t@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        Mockito.when(solutionRepository.findById(solutionId))
                .thenReturn(Optional.of(solution));

        Mockito.when(gradeRepository.findBySolution_Id(solutionId))
                .thenReturn(Optional.empty());

        GradeDto result = gradeService.upsertGrade(token, solutionId, request);

        assertEquals(5, result.getScore());
        assertEquals("Good", result.getTeacherComment());
        assertEquals(teacherId, result.getTeacherId());
    }

    @Test
    void updateGrade_success() {

        GradeUpsertRequest request = new GradeUpsertRequest(4, "Updated");

        SolutionEntity solution = new SolutionEntity();
        solution.setId(solutionId);

        GradeEntity existing = new GradeEntity();
        existing.setSolution(solution);

        UserPrincipal teacher = new UserPrincipal(teacherId, "t@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        Mockito.when(solutionRepository.findById(solutionId))
                .thenReturn(Optional.of(solution));

        Mockito.when(gradeRepository.findBySolution_Id(solutionId))
                .thenReturn(Optional.of(existing));

        GradeDto result = gradeService.upsertGrade(token, solutionId, request);

        assertEquals(4, result.getScore());
        assertEquals("Updated", result.getTeacherComment());
    }

    @Test
    void invalidToken_throws401() {

        GradeUpsertRequest request = new GradeUpsertRequest(5, "test");

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> gradeService.upsertGrade(token, solutionId, request)
        );
    }

    @Test
    void notTeacher_throws403() {

        GradeUpsertRequest request = new GradeUpsertRequest(5, "test");

        UserPrincipal student = new UserPrincipal(UUID.randomUUID(), "s@mail.com", Role.STUDENT);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(student);

        assertThrows(
                ForbiddenException.class,
                () -> gradeService.upsertGrade(token, solutionId, request)
        );
    }

    @Test
    void solutionNotFound_throws404() {

        GradeUpsertRequest request = new GradeUpsertRequest(5, "test");

        UserPrincipal teacher = new UserPrincipal(teacherId, "t@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        Mockito.when(solutionRepository.findById(solutionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> gradeService.upsertGrade(token, solutionId, request)
        );
    }

    @Test
    void badScore_throws400() {

        GradeUpsertRequest request = new GradeUpsertRequest(10, "bad");

        UserPrincipal teacher = new UserPrincipal(teacherId, "t@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        assertThrows(
                BadRequestException.class,
                () -> gradeService.upsertGrade(token, solutionId, request)
        );
    }
}