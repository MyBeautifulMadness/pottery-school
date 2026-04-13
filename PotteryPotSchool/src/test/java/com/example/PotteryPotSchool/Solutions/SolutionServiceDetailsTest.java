//package com.example.PotteryPotSchool.Solutions;
//
//import com.example.PotteryPotSchool.config.*;
//import com.example.PotteryPotSchool.dto.Solutions.SolutionDetailsDto;
//import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
//import com.example.PotteryPotSchool.entity.Posts.PostEntity;
//import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
//import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
//import com.example.PotteryPotSchool.enums.Users.Role;
//import com.example.PotteryPotSchool.repository.GradeRepository;
//import com.example.PotteryPotSchool.repository.SolutionRepository;
//import com.example.PotteryPotSchool.security.UserPrincipal;
//import com.example.PotteryPotSchool.service.Login.JwtService;
//import com.example.PotteryPotSchool.service.Solutions.impl.SolutionServiceImpl;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class SolutionServiceDetailsTest {
//
//    @Mock
//    private SolutionRepository solutionRepository;
//
//    @Mock
//    private GradeRepository gradeRepository;
//
//    @Mock
//    private JwtService jwtService;
//
//    @InjectMocks
//    private SolutionServiceImpl solutionService;
//
//    private UUID solutionId;
//    private UUID studentId;
//    private UUID teacherId;
//    private String token;
//
//    @BeforeEach
//    void setup() {
//
//        MockitoAnnotations.openMocks(this);
//
//        solutionId = UUID.randomUUID();
//        studentId = UUID.randomUUID();
//        teacherId = UUID.randomUUID();
//        token = "valid-token";
//    }
//
//    @Test
//    void teacherCanViewSolution() {
//
//        UserPrincipal teacher = new UserPrincipal(teacherId,"t@mail", Role.TEACHER);
//
//        PostEntity post = new PostEntity();
//        post.setId(UUID.randomUUID());
//
//        SolutionEntity solution = SolutionEntity.builder()
//                .id(solutionId)
//                .post(post)
//                .studentId(studentId)
//                .status(SolutionStatus.SUBMITTED)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        GradeEntity grade = GradeEntity.builder()
//                .score(5)
//                .teacherId(teacherId)
//                .gradedAt(LocalDateTime.now())
//                .build();
//
//        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
//        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);
//
//        Mockito.when(solutionRepository.findById(solutionId))
//                .thenReturn(Optional.of(solution));
//
//        Mockito.when(gradeRepository.findBySolution_Id(solutionId))
//                .thenReturn(Optional.of(grade));
//
//        SolutionDetailsDto result = solutionService.getSolution(token, solutionId);
//
//        assertEquals(solutionId, result.getId());
//        assertEquals(5, result.getGrade().getScore());
//    }
//
//    @Test
//    void studentCanViewOwnSolution() {
//
//        UserPrincipal student = new UserPrincipal(studentId,"s@mail", Role.STUDENT);
//
//        PostEntity post = new PostEntity();
//        post.setId(UUID.randomUUID());
//
//        SolutionEntity solution = SolutionEntity.builder()
//                .id(solutionId)
//                .post(post)
//                .studentId(studentId)
//                .status(SolutionStatus.DRAFT)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
//        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(student);
//
//        Mockito.when(solutionRepository.findById(solutionId))
//                .thenReturn(Optional.of(solution));
//
//        Mockito.when(gradeRepository.findBySolution_Id(solutionId))
//                .thenReturn(Optional.empty());
//
//        SolutionDetailsDto result = solutionService.getSolution(token, solutionId);
//
//        assertEquals(solutionId, result.getId());
//        assertNull(result.getGrade());
//    }
//
//    @Test
//    void studentCannotViewOthersSolution() {
//
//        UserPrincipal student = new UserPrincipal(UUID.randomUUID(),"s@mail", Role.STUDENT);
//
//        PostEntity post = new PostEntity();
//        post.setId(UUID.randomUUID());
//
//        SolutionEntity solution = SolutionEntity.builder()
//                .id(solutionId)
//                .post(post)
//                .studentId(studentId)
//                .build();
//
//        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
//        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(student);
//
//        Mockito.when(solutionRepository.findById(solutionId))
//                .thenReturn(Optional.of(solution));
//
//        assertThrows(
//                ForbiddenException.class,
//                () -> solutionService.getSolution(token, solutionId)
//        );
//    }
//
//    @Test
//    void invalidTokenThrows401() {
//
//        Mockito.when(jwtService.isTokenValid(token)).thenReturn(false);
//
//        assertThrows(
//                UnauthorizedException.class,
//                () -> solutionService.getSolution(token, solutionId)
//        );
//    }
//
//    @Test
//    void solutionNotFoundThrows404() {
//
//        UserPrincipal teacher = new UserPrincipal(teacherId,"t@mail", Role.TEACHER);
//
//        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
//        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);
//
//        Mockito.when(solutionRepository.findById(solutionId))
//                .thenReturn(Optional.empty());
//
//        assertThrows(
//                NotFoundException.class,
//                () -> solutionService.getSolution(token, solutionId)
//        );
//    }
//}
