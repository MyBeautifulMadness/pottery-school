package com.example.PotteryPotSchool.service.Students.impl;

import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Students.StudentDetailsDto;
import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.service.Login.JwtService;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Students.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public List<StudentSummaryDto> getStudents(String token, String q, Integer page, Integer size) {

        if (token == null || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedException("Invalid or missing token");
        }

        UserPrincipal principal = jwtService.extractUserPrincipal(token);

        if (principal.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Access denied");
        }

        if (page < 0 || size <= 0 || size > 100) {
            throw new BadRequestException("Invalid pagination parameters");
        }

        List<UserEntity> students;

        if (q == null || q.isBlank()) {
            students = userRepository.findByRole(Role.STUDENT);
        } else {
            students = userRepository
                    .findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                            Role.STUDENT, q,
                            Role.STUDENT, q);
        }

        int fromIndex = page * size;

        if (fromIndex >= students.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + size, students.size());

        return students.subList(fromIndex, toIndex)
                .stream()
                .map(s -> new StudentSummaryDto(
                        s.getId(),
                        s.getFullName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public StudentDetailsDto getStudentById(String token, UUID studentId) {

        if (token == null || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedException("Invalid token");
        }

        UserPrincipal principal = jwtService.extractUserPrincipal(token);

        if (principal.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Access denied");
        }

        UserEntity student = userRepository
                .findByIdAndRole(studentId, Role.STUDENT)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        return new StudentDetailsDto(
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                student.getAbout()
        );
    }
}
