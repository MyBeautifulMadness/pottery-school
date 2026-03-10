package com.example.PotteryPotSchool.Grades;

import com.example.PotteryPotSchool.config.*;
import com.example.PotteryPotSchool.dto.Grades.GradeDto;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GradeServiceGetTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private SolutionRepository solutionRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private UUID solutionId;
    private UUID studentId;
    private UUID teacherId;
    private String token;

    @BeforeEach
    void setup() {

        MockitoAnnotations.openMocks(this);

        solutionId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        token = "valid-token";
    }

    @Test
    void teacherCanViewAnyGrade() {

        UserPrincipal teacher = new UserPrincipal(teacherId, "t@mail.com", Role.TEACHER);

        SolutionEntity solution = new SolutionEntity();
        solution.setId(solutionId);
        solution.setStudentId(studentId);

        GradeEntity grade = GradeEntity.builder()
                .solution(solution)
                .score(5)
                .teacherId(teacherId)
                .teacherComment("Good")
                .gradedAt(LocalDateTime.now())
                .build();

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        Mockito.when(solutionRepository.findById(solutionId))
                .thenReturn(Optional.of(solution));

        Mockito.when(gradeRepository.findBySolution_Id(solutionId))
                .thenReturn(Optional.of(grade));

        GradeDto result = gradeService.getGrade(token, solutionId);

        assertEquals(5, result.getScore());
        assertEquals("Good", result.getTeacherComment());
    }

    @Test
    void studentCanViewOwnGrade() {

        UserPrincipal student = new UserPrincipal(studentId, "s@mail.com", Role.STUDENT);

        SolutionEntity solution = new SolutionEntity();
        solution.setId(solutionId);
        solution.setStudentId(studentId);

        GradeEntity grade = GradeEntity.builder()
                .solution(solution)
                .score(4)
                .teacherComment("Ok")
                .teacherId(teacherId)
                .gradedAt(LocalDateTime.now())
                .build();

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(student);

        Mockito.when(solutionRepository.findById(solutionId))
                .thenReturn(Optional.of(solution));

        Mockito.when(gradeRepository.findBySolution_Id(solutionId))
                .thenReturn(Optional.of(grade));

        GradeDto result = gradeService.getGrade(token, solutionId);

        assertEquals(4, result.getScore());
    }

    @Test
    void studentCannotViewOthersGrade() {

        UserPrincipal student = new UserPrincipal(UUID.randomUUID(), "s@mail.com", Role.STUDENT);

        SolutionEntity solution = new SolutionEntity();
        solution.setId(solutionId);
        solution.setStudentId(studentId);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(student);

        Mockito.when(solutionRepository.findById(solutionId))
                .thenReturn(Optional.of(solution));

        assertThrows(
                ForbiddenException.class,
                () -> gradeService.getGrade(token, solutionId)
        );
    }

    @Test
    void invalidTokenThrows401() {

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> gradeService.getGrade(token, solutionId)
        );
    }

    @Test
    void solutionNotFoundThrows404() {

        UserPrincipal teacher = new UserPrincipal(teacherId, "t@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        Mockito.when(solutionRepository.findById(solutionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> gradeService.getGrade(token, solutionId)
        );
    }

    @Test
    void gradeNotFoundThrows404() {

        UserPrincipal teacher = new UserPrincipal(teacherId, "t@mail.com", Role.TEACHER);

        SolutionEntity solution = new SolutionEntity();
        solution.setId(solutionId);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        Mockito.when(solutionRepository.findById(solutionId))
                .thenReturn(Optional.of(solution));

        Mockito.when(gradeRepository.findBySolution_Id(solutionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> gradeService.getGrade(token, solutionId)
        );
    }
}
