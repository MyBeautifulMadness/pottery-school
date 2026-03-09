package com.example.PotteryPotSchool.Students;

import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.service.Login.impl.JwtServiceImpl;
import com.example.PotteryPotSchool.service.Students.impl.StudentServiceImpl;
import com.example.PotteryPotSchool.security.UserPrincipal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtServiceImpl jwtService;

    @InjectMocks
    private StudentServiceImpl studentService;

    private String token;

    @BeforeEach
    void setup() {
        token = "valid-token";
    }

    @Test
    void getStudents_success_returnsPaginatedList() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        UserEntity s1 = new UserEntity();
        s1.setId(UUID.randomUUID());
        s1.setFullName("Alice");
        s1.setEmail("a@mail.com");
        s1.setRole(Role.STUDENT);

        UserEntity s2 = new UserEntity();
        s2.setId(UUID.randomUUID());
        s2.setFullName("Bob");
        s2.setEmail("b@mail.com");
        s2.setRole(Role.STUDENT);

        Mockito.when(userRepository.findByRole(Role.STUDENT))
                .thenReturn(List.of(s1, s2));

        List<StudentSummaryDto> result =
                studentService.getStudents(token, null, 0, 20);

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getFullName());
    }

    @Test
    void getStudents_searchQuery_filtersCorrectly() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        UserEntity student = new UserEntity();
        student.setId(UUID.randomUUID());
        student.setFullName("Charlie");
        student.setEmail("c@mail.com");
        student.setRole(Role.STUDENT);

        Mockito.when(userRepository
                        .findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                                Role.STUDENT, "char",
                                Role.STUDENT,"char"))
                .thenReturn(List.of(student));

        List<StudentSummaryDto> result =
                studentService.getStudents(token, "char", 0, 20);

        assertEquals(1, result.size());
        assertEquals("Charlie", result.get(0).getFullName());
    }

    @Test
    void getStudents_pagination_returnsCorrectPage() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        UserEntity s1 = new UserEntity();
        s1.setId(UUID.randomUUID());
        s1.setFullName("Alice");

        UserEntity s2 = new UserEntity();
        s2.setId(UUID.randomUUID());
        s2.setFullName("Bob");

        UserEntity s3 = new UserEntity();
        s3.setId(UUID.randomUUID());
        s3.setFullName("Charlie");

        Mockito.when(userRepository.findByRole(Role.STUDENT))
                .thenReturn(List.of(s1, s2, s3));

        List<StudentSummaryDto> result =
                studentService.getStudents(token, null, 1, 2);

        assertEquals(1, result.size());
        assertEquals("Charlie", result.get(0).getFullName());
    }

    @Test
    void getStudents_invalidToken_throwsUnauthorized() {

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> studentService.getStudents(token, null, 0, 20)
        );
    }

    @Test
    void getStudents_notTeacher_throwsForbidden() {

        UserPrincipal student =
                new UserPrincipal(UUID.randomUUID(), "s@mail.com", Role.STUDENT);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(student);

        assertThrows(
                ForbiddenException.class,
                () -> studentService.getStudents(token, null, 0, 20)
        );
    }

    @Test
    void getStudents_invalidPagination_throwsBadRequest() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        assertThrows(
                BadRequestException.class,
                () -> studentService.getStudents(token, null, -1, 20)
        );
    }
}