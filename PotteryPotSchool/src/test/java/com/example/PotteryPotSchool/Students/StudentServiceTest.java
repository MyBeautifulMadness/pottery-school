package com.example.PotteryPotSchool.Students;

import com.example.PotteryPotSchool.dto.Students.PageResponse;
import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import com.example.PotteryPotSchool.enums.Users.Role;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

        StudentSummaryDto s1 =
                new StudentSummaryDto(UUID.randomUUID(), "Alice");

        StudentSummaryDto s2 =
                new StudentSummaryDto(UUID.randomUUID(), "Bob");

        Page<StudentSummaryDto> page =
                new PageImpl<>(List.of(s1, s2));

        Mockito.when(userRepository.findStudents(Mockito.any(PageRequest.class)))
                .thenReturn(page);

        PageResponse<StudentSummaryDto> result =
                studentService.getStudents(token, null, PageRequest.of(0, 20));

        assertEquals(2, result.getItems().size());
        assertEquals("Alice", result.getItems().get(0).getFullName());
    }

    @Test
    void getStudents_searchQuery_filtersCorrectly() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        StudentSummaryDto student =
                new StudentSummaryDto(UUID.randomUUID(), "Charlie");

        Page<StudentSummaryDto> page =
                new PageImpl<>(List.of(student));

        Mockito.when(userRepository.searchStudents(Mockito.eq("char"), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        PageResponse<StudentSummaryDto> result =
                studentService.getStudents(token,"char", PageRequest.of(0, 20));

        assertEquals(1, result.getItems().size());
        assertEquals("Charlie", result.getItems().get(0).getFullName());
    }

    @Test
    void getStudents_pagination_returnsCorrectPage() {

        UserPrincipal teacher =
                new UserPrincipal(UUID.randomUUID(), "teacher@mail.com", Role.TEACHER);

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtService.extractUserPrincipal(token)).thenReturn(teacher);

        StudentSummaryDto s3 =
                new StudentSummaryDto(UUID.randomUUID(), "Charlie");

        Page<StudentSummaryDto> page =
                new PageImpl<>(List.of(s3));

        Mockito.when(userRepository.findStudents(Mockito.any(PageRequest.class)))
                .thenReturn(page);

        PageResponse<StudentSummaryDto> result =
                studentService.getStudents(token,null, PageRequest.of(0, 20));

        assertEquals(1, result.getItems().size());
        assertEquals("Charlie", result.getItems().get(0).getFullName());
    }

    @Test
    void getStudents_invalidToken_throwsUnauthorized() {

        Mockito.when(jwtService.isTokenValid(token)).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> studentService.getStudents(token,null, PageRequest.of(0, 20))
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
                () -> studentService.getStudents(token,null, PageRequest.of(0, 20))
        );
    }
}