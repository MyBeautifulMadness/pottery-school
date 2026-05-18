//package com.example.PotteryPotSchool.Students;
//
//import com.example.PotteryPotSchool.config.*;
//import com.example.PotteryPotSchool.dto.Students.PerformanceSummaryDto;
//import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
//import com.example.PotteryPotSchool.entity.Posts.PostEntity;
//import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
//import com.example.PotteryPotSchool.repository.*;
//import com.example.PotteryPotSchool.service.Login.JwtService;
//import com.example.PotteryPotSchool.service.Students.impl.StudentServiceImpl;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class StudentPerformanceServiceTest {
//
//    @Mock
//    private JwtService jwtService;
//
//    @Mock
//    private PostRepository postRepository;
//
//    @Mock
//    private SolutionRepository solutionRepository;
//
//    @Mock
//    private GradeRepository gradeRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private StudentServiceImpl studentService;
//
//    private UUID studentId;
//    private String token;
//
//    @BeforeEach
//    void setup() {
//
//        MockitoAnnotations.openMocks(this);
//
//        studentId = UUID.randomUUID();
//        token = "valid-token";
//    }
//
//    @Test
//    void performanceCalculatedCorrectly() {
//
//        PostEntity post = new PostEntity();
//        post.setId(UUID.randomUUID());
//        post.setTitle("Task 1");
//
//        SolutionEntity solution = SolutionEntity.builder()
//                .id(UUID.randomUUID())
//                .studentId(studentId)
//                .build();
//
//        GradeEntity grade = GradeEntity.builder()
//                .score(5)
//                .teacherId(UUID.randomUUID())
//                .gradedAt(LocalDateTime.now())
//                .build();
//
//        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
//        Mockito.when(userRepository.existsById(studentId)).thenReturn(true);
//
//        Mockito.when(postRepository.findAll())
//                .thenReturn(List.of(post));
//
//        Mockito.when(solutionRepository.findByPostIdAndStudentId(post.getId(), studentId))
//                .thenReturn(Optional.of(solution));
//
//        Mockito.when(gradeRepository.findBySolution_Id(solution.getId()))
//                .thenReturn(Optional.of(grade));
//
//        PerformanceSummaryDto result =
//                studentService.getStudentPerformance(token, studentId);
//
//        assertEquals(studentId, result.getStudentId());
//        assertEquals(5.0, result.getAverageGrade());
//        assertEquals(1, result.getItems().size());
//    }
//
//    @Test
//    void averageNullIfNoGrades() {
//
//        PostEntity post = new PostEntity();
//        post.setId(UUID.randomUUID());
//        post.setTitle("Task");
//
//        SolutionEntity solution = SolutionEntity.builder()
//                .id(UUID.randomUUID())
//                .studentId(studentId)
//                .build();
//
//        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
//        Mockito.when(userRepository.existsById(studentId)).thenReturn(true);
//
//        Mockito.when(postRepository.findAll())
//                .thenReturn(List.of(post));
//
//        Mockito.when(solutionRepository.findByPostIdAndStudentId(post.getId(), studentId))
//                .thenReturn(Optional.of(solution));
//
//        Mockito.when(gradeRepository.findBySolution_Id(solution.getId()))
//                .thenReturn(Optional.empty());
//
//        PerformanceSummaryDto result =
//                studentService.getStudentPerformance(token, studentId);
//
//        assertNull(result.getAverageGrade());
//    }
//
//    @Test
//    void invalidTokenThrows401() {
//
//        Mockito.when(jwtService.isTokenValid(token)).thenReturn(false);
//
//        assertThrows(
//                UnauthorizedException.class,
//                () -> studentService.getStudentPerformance(token, studentId)
//        );
//    }
//
//    @Test
//    void studentNotFoundThrows404() {
//
//        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
//        Mockito.when(userRepository.existsById(studentId)).thenReturn(false);
//
//        assertThrows(
//                NotFoundException.class,
//                () -> studentService.getStudentPerformance(token, studentId)
//        );
//    }
//}
