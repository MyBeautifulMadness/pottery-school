package com.example.PotteryPotSchool.service.Grades.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Grades.*;
import com.example.PotteryPotSchool.dto.Solutions.MemberGradeDto;
import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.entity.Teams.TeamEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Solutions.SolutionOwnerType;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.GradeRepository;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.repository.TeamRepository;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Grades.GradeService;
import com.example.PotteryPotSchool.service.Login.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final SolutionRepository solutionRepository;
    private final JwtService jwtService;
    private final TeamRepository teamRepository;

    @Override
    public SolutionGradeDto upsertGrade(String token, UUID solutionId, GradeUpsertRequest request) {

        validateToken(token);

        UserPrincipal user = jwtService.extractUserPrincipal(token);

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только учитель может оценивать участников решения");
        }

        validateGradeRequest(request);

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено"));

        List<UUID> memberIds = getSolutionMemberIds(solution);

        if (memberIds.isEmpty()) {
            throw new BadRequestException("В решении нет студентов для оценки");
        }

        LocalDateTime now = LocalDateTime.now();

        List<GradeEntity> gradesToSave = memberIds.stream()
                .map(studentId -> {
                    GradeEntity grade = gradeRepository.findBySolution_IdAndStudentId(solutionId, studentId)
                            .orElseGet(() -> GradeEntity.builder()
                                    .solution(solution)
                                    .studentId(studentId)
                                    .build());

                    grade.setScore(request.getScore());
                    grade.setTeacherComment(request.getTeacherComment());
                    grade.setTeacherId(user.getId());
                    grade.setGradedAt(now);

                    return grade;
                })
                .toList();

        List<GradeEntity> savedGrades = gradeRepository.saveAll(gradesToSave);

        return SolutionGradeDto.builder()
                .solutionId(solutionId)
                .grades(savedGrades.stream()
                        .map(this::mapToStudentGradeDto)
                        .toList())
                .build();
    }

    @Override
    public SolutionGradeDto getGrade(String token, UUID solutionId) {

        validateToken(token);

        UserPrincipal user = jwtService.extractUserPrincipal(token);

        if (user.getRole() != Role.STUDENT && user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Доступ запрещен");
        }

        solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено"));

        List<GradeEntity> grades = gradeRepository.findAllBySolution_Id(solutionId);

        if (grades.isEmpty()) {
            throw new NotFoundException("Оценки не найдены");
        }

        return SolutionGradeDto.builder()
                .solutionId(solutionId)
                .grades(grades.stream()
                        .map(this::mapToStudentGradeDto)
                        .toList())
                .build();
    }

    @Override
    public MemberGradeDto upsertMemberGrade(String token, UUID solutionId, UUID studentId, GradeUpsertRequest request) {

        validateToken(token);

        UserPrincipal user = jwtService.extractUserPrincipal(token);

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только учитель может оценивать участников");
        }

        validateGradeRequest(request);

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено"));

        List<UUID> memberIds = getSolutionMemberIds(solution);

        if (!memberIds.contains(studentId)) {
            throw new BadRequestException("Студент не является участником этого решения");
        }

        GradeEntity grade = gradeRepository.findBySolution_IdAndStudentId(solutionId, studentId)
                .orElseGet(() -> GradeEntity.builder()
                        .solution(solution)
                        .studentId(studentId)
                        .build());

        grade.setScore(request.getScore());
        grade.setTeacherComment(request.getTeacherComment());
        grade.setTeacherId(user.getId());
        grade.setGradedAt(LocalDateTime.now());

        GradeEntity savedGrade = gradeRepository.save(grade);

        return mapToMemberGradeDto(savedGrade);
    }

    @Override
    public MemberGradeDto getMemberGrade(String token, UUID solutionId, UUID studentId) {

        validateToken(token);

        UserPrincipal user = jwtService.extractUserPrincipal(token);

        if (user.getRole() != Role.STUDENT && user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Доступ запрещен");
        }

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено"));

        List<UUID> memberIds = getSolutionMemberIds(solution);

        if (!memberIds.contains(studentId)) {
            throw new BadRequestException("Студент не является участником этого решения");
        }

        GradeEntity grade = gradeRepository.findBySolution_IdAndStudentId(solutionId, studentId)
                .orElseThrow(() -> new NotFoundException("Оценки не найдены"));

        return mapToMemberGradeDto(grade);
    }

    @Override
    public StudentPerformanceDto getStudentPerformance(String token, UUID studentId) {

        validateToken(token);

        UserPrincipal user = jwtService.extractUserPrincipal(token);

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только учитель может просматривать успеваемость учащихся");
        }

        List<GradeEntity> grades = gradeRepository.findAllByStudentId(studentId);

        if (grades.isEmpty()) {
            throw new NotFoundException("Оценки для студента не найдены");
        }

        return StudentPerformanceDto.builder()
                .studentId(studentId)
                .grades(grades.stream()
                        .map(this::mapToStudentPerformanceItemDto)
                        .toList())
                .build();
    }

    private void validateToken(String token) {
        if (token == null || token.isBlank() || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedException("Недопустимый токен");
        }
    }

    private void validateGradeRequest(GradeUpsertRequest request) {
        if (request == null) {
            throw new BadRequestException("Введите данные к запросу");
        }

        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            throw new BadRequestException("Оценка должна быть от 1 до 5");
        }
    }

    private StudentGradeDto mapToStudentGradeDto(GradeEntity grade) {
        return StudentGradeDto.builder()
                .studentId(grade.getStudentId())
                .score(grade.getScore())
                .teacherComment(grade.getTeacherComment())
                .gradedAt(grade.getGradedAt())
                .teacherId(grade.getTeacherId())
                .build();
    }

    private MemberGradeDto mapToMemberGradeDto(GradeEntity grade) {
        return MemberGradeDto.builder()
                .solutionId(grade.getSolution().getId())
                .studentId(grade.getStudentId())
                .score(grade.getScore())
                .teacherComment(grade.getTeacherComment())
                .gradedAt(grade.getGradedAt())
                .teacherId(grade.getTeacherId())
                .build();
    }

    private StudentPerformanceItemDto mapToStudentPerformanceItemDto(GradeEntity grade) {
        return StudentPerformanceItemDto.builder()
                .solutionId(grade.getSolution().getId())
                .score(grade.getScore())
                .teacherComment(grade.getTeacherComment())
                .gradedAt(grade.getGradedAt())
                .teacherId(grade.getTeacherId())
                .build();
    }

    private List<UUID> getSolutionMemberIds(SolutionEntity solution) {
        if (solution.getOwnerType() == SolutionOwnerType.STUDENT) {
            if (solution.getStudentId() == null) {
                throw new BadRequestException("Решение для студентов не содержит studentId");
            }
            return List.of(solution.getStudentId());
        }

        if (solution.getOwnerType() == SolutionOwnerType.TEAM) {
            if (solution.getTeamId() == null) {
                throw new BadRequestException("Командное решение не содержит teamId");
            }

            TeamEntity team = teamRepository.findById(solution.getTeamId())
                    .orElseThrow(() -> new NotFoundException("Команда не найдена"));

            List<UUID> memberIds = team.getMembers().stream()
                    .map(UserEntity::getId)
                    .toList();

            if (memberIds.isEmpty()) {
                throw new BadRequestException("В команде нет студентов");
            }

            return memberIds;
        }

        throw new BadRequestException("Unsupported solution owner type");
    }
}
