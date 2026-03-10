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

        UserPrincipal principal = jwtService.extractUserPrincipal(token);

        if (principal.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Access denied");
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

    @Override
    public PerformanceSummaryDto getStudentPerformance(String token, UUID studentId) {

        if (token == null) {
            throw new UnauthorizedException("Invalid token");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtService.isTokenValid(token)) {
            throw new UnauthorizedException("Invalid token");
        }

        if (!userRepository.existsById(studentId)) {
            throw new NotFoundException("Student not found");
        }

        List<PostEntity> posts = postRepository.findAll();

        List<PerformanceItemDto> items = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();

        for (PostEntity post : posts) {

            Optional<SolutionEntity> solutionOpt =
                    solutionRepository.findByPostIdAndStudentId(post.getId(), studentId);

            if (solutionOpt.isEmpty()) {
                continue; // просто пропускаем пост
            }

            SolutionEntity solution = solutionOpt.get();

            GradeDto gradeDto = gradeRepository.findBySolution_Id(solution.getId())
                    .map(grade -> {

                        scores.add(grade.getScore());

                        return GradeDto.builder()
                                .solutionId(solution.getId())
                                .score(grade.getScore())
                                .teacherComment(grade.getTeacherComment())
                                .gradedAt(grade.getGradedAt())
                                .teacherId(grade.getTeacherId())
                                .build();
                    })
                    .orElse(null);

            items.add(
                    PerformanceItemDto.builder()
                            .postId(post.getId())
                            .postTitle(post.getTitle())
                            .solutionId(solution.getId())
                            .grade(gradeDto)
                            .build()
            );
        }

        Double avg = null;

        if (!scores.isEmpty()) {
            avg = scores.stream()
                    .mapToInt(i -> i)
                    .average()
                    .orElse(0);
        }

        return PerformanceSummaryDto.builder()
                .studentId(studentId)
                .averageGrade(avg)
                .items(items)
                .build();
    }
}
