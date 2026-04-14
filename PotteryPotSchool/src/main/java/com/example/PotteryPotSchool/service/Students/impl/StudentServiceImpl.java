package com.example.PotteryPotSchool.service.Students.impl;

import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Grades.GradeDto;
import com.example.PotteryPotSchool.dto.Profiles.Profile;
import com.example.PotteryPotSchool.dto.Students.*;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Profiles.ProfileEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.repository.*;
import com.example.PotteryPotSchool.service.Login.JwtService;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Students.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final SolutionRepository solutionRepository;
    private final GradeRepository gradeRepository;

    @Override
    public PageResponse<StudentSummaryDto> getStudents(
            String token,
            String query,
            Pageable pageable
    ) {

        if (token == null || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedException("Invalid token");
        }

        UserPrincipal principal = jwtService.extractUserPrincipal(token);

        if (principal.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Access denied");
        }

        Page<StudentSummaryDto> page;

        if (query == null || query.isBlank()) {
            page = userRepository.findStudents(pageable);
        } else {
            page = userRepository.searchStudents(query, pageable);
        }

        return PageResponse.<StudentSummaryDto>builder()
                .items(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .build();
    }

    @Override
    public StudentDetailsDto getStudentById(String token, UUID studentId) {

        if (token == null || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedException("Invalid token");
        }


        UserEntity student = userRepository
                .findByIdAndRole(studentId, Role.STUDENT)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        ProfileEntity profileEntity = profileRepository
                .findById(studentId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        Profile profile = Profile.builder()
                .userId(profileEntity.getUserId())
                .fullName(profileEntity.getFullName())
                .about(profileEntity.getAbout())
                .build();

        return new StudentDetailsDto(
                student.getId(),
                profile
        );
    }
}
