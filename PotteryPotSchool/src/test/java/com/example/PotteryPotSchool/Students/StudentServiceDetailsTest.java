package com.example.PotteryPotSchool.Students;

import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Students.StudentDetailsDto;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Login.JwtService;
import com.example.PotteryPotSchool.service.Students.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceDetailsTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private StudentServiceImpl studentService;

    private UUID studentId;
    private String token;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        token = "valid-token";
    }

    @Test
    void getStudentById_success_returnsDetails() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        UserEntity student = new UserEntity();
        student.setId(studentId);
        student.setEmail("student@test.com");
        student.setFullName("John Doe");
        student.setPassword("pass");
        student.setAbout("About John");
        student.setRole(Role.STUDENT);

        Mockito.when(userRepository.findByIdAndRole(studentId, Role.STUDENT))
                .thenReturn(Optional.of(student));

        StudentDetailsDto dto =
                studentService.getStudentById(token, studentId);

        assertNotNull(dto);
        assertEquals(studentId, dto.getId());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("student@test.com", dto.getEmail());
        assertEquals("About John", dto.getAbout());
    }

    @Test
    void getStudentById_studentNotFound() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        Mockito.when(userRepository.findByIdAndRole(studentId, Role.STUDENT))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> studentService.getStudentById(token, studentId)
        );
    }

    @Test
    void getStudentById_notTeacher() {

        UserPrincipal studentPrincipal =
                new UserPrincipal(UUID.randomUUID(), "s@mail.com", Role.STUDENT);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(studentPrincipal);

        assertThrows(
                ForbiddenException.class,
                () -> studentService.getStudentById(token, studentId)
        );
    }

    @Test
    void getStudentById_invalidToken() {

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> studentService.getStudentById(token, studentId)
        );
    }
}
