package com.example.PotteryPotSchool.service.Grades.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Grades.GradeDto;
import com.example.PotteryPotSchool.dto.Grades.GradeUpsertRequest;
import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.GradeRepository;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Grades.GradeService;
import com.example.PotteryPotSchool.service.Login.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final SolutionRepository solutionRepository;
    private final JwtService jwtService;

    @Override
    public GradeDto upsertGrade(String token, UUID solutionId, GradeUpsertRequest request) {

        if (token == null || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedException("Invalid token");
        }

        UserPrincipal user = jwtService.extractUserPrincipal(token);

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only teacher can grade");
        }

        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            throw new BadRequestException("Score must be between 1 and 5");
        }

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Solution not found"));

        GradeEntity grade = gradeRepository.findBySolution_Id(solutionId)
                .orElse(
                        GradeEntity.builder()
                                .solution(solution)
                                .build()
                );

        grade.setScore(request.getScore());
        grade.setTeacherComment(request.getTeacherComment());
        grade.setTeacherId(user.getId());
        grade.setGradedAt(LocalDateTime.now());

        gradeRepository.save(grade);

        return GradeDto.builder()
                .solutionId(solutionId)
                .score(grade.getScore())
                .teacherComment(grade.getTeacherComment())
                .gradedAt(grade.getGradedAt())
                .teacherId(grade.getTeacherId())
                .build();
    }
}
