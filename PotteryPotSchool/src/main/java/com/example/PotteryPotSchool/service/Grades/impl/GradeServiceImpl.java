package com.example.PotteryPotSchool.service.Grades.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.config.UnauthorizedException;
import com.example.PotteryPotSchool.dto.Criteria.CriterionDto;
import com.example.PotteryPotSchool.dto.Grades.*;
import com.example.PotteryPotSchool.dto.Solutions.MemberGradeDto;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Grades.CriterionEntity;
import com.example.PotteryPotSchool.entity.Grades.CriterionGradeItemEntity;
import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
import com.example.PotteryPotSchool.entity.Grades.SelfAssessmentItemEntity;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.entity.Teams.TeamEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Grades.CriterionImpactType;
import com.example.PotteryPotSchool.enums.Grades.GradeSource;
import com.example.PotteryPotSchool.enums.Grades.CriterionValueType;
import com.example.PotteryPotSchool.enums.Solutions.SolutionOwnerType;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.CriterionGradeItemRepository;
import com.example.PotteryPotSchool.repository.CriterionRepository;
import com.example.PotteryPotSchool.repository.GradeRepository;
import com.example.PotteryPotSchool.repository.SelfAssessmentItemRepository;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.repository.TeamRepository;
import com.example.PotteryPotSchool.service.Grades.GradeService;
import com.example.PotteryPotSchool.service.Login.JwtService;
import com.example.PotteryPotSchool.service.Me.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final SolutionRepository solutionRepository;
    private final JwtService jwtService;
    private final TeamRepository teamRepository;
    private final CriterionRepository criterionRepository;
    private final CriterionGradeItemRepository criterionGradeItemRepository;
    private final SelfAssessmentItemRepository selfAssessmentItemRepository;
    private final MeService meService;

    @Override
    @Transactional
    public SolutionGradeDto upsertGrade(UUID solutionId, GradeUpsertRequest request) {

        User user = meService.getMe();

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только учитель может оценивать участников решения");
        }

        if (request == null || request.getScore() == null) {
            throw new BadRequestException("Оценка обязательна");
        }

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено"));

        TaskEntity task = solution.getPost().getTask();
        boolean criterionGradingEnabled = task != null && Boolean.TRUE.equals(task.getGradingEnabled());

        if (criterionGradingEnabled) {
            validateManualFinalScoreForCriterionGrade(request, task);
        } else {
            validateSimpleGradeRequest(request);
        }

        List<UUID> memberIds = getSolutionMemberIds(solution);

        if (memberIds.isEmpty()) {
            throw new BadRequestException("В решении нет студентов для оценки");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal manualFinalScore = BigDecimal.valueOf(request.getScore());

        solution.setTeamGrade(request.getScore());
        solutionRepository.save(solution);

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
                    grade.setSource(GradeSource.TEACHER);
                    grade.setGradedAt(now);

                    if (criterionGradingEnabled) {
                        grade.setMaxFinalScore(task.getMaxFinalScore());
                        grade.setFinalScore(manualFinalScore);
                        grade.setRawScore(manualFinalScore);
                    }

                    return grade;
                })
                .toList();

        List<GradeEntity> savedGrades = gradeRepository.saveAll(gradesToSave);

        return SolutionGradeDto.builder()
                .solutionId(solutionId)
                .teamGrade(solution.getTeamGrade())
                .grades(savedGrades.stream()
                        .map(this::mapToStudentGradeDto)
                        .toList())
                .build();
    }

    private void validateSimpleGradeRequest(GradeUpsertRequest request) {
        if (request == null) {
            throw new BadRequestException("123");
        }
    }

    private void validateManualFinalScoreForCriterionGrade(GradeUpsertRequest request, TaskEntity task) {
        if (request == null) {
            throw new BadRequestException("Введите данные к запросу");
        }

        if (request.getScore() == null) {
            throw new BadRequestException("Итоговая оценка обязательна");
        }

        BigDecimal score = BigDecimal.valueOf(request.getScore());
        BigDecimal maxFinalScore = safe(task.getMaxFinalScore());

        if (score.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Итоговая оценка не может быть меньше 0");
        }

        if (maxFinalScore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("У задания некорректная максимальная итоговая оценка");
        }

        if (score.compareTo(maxFinalScore) > 0) {
            throw new BadRequestException("Итоговая оценка не может быть больше maxFinalScore");
        }
    }

    @Override
    public SolutionGradeDto getGrade(UUID solutionId) {

        User user = meService.getMe();

        if (user.getRole() != Role.STUDENT && user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Access denied");
        }

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Solution not found"));

        List<GradeEntity> grades = gradeRepository.findAllBySolution_Id(solutionId);

        if (grades.isEmpty()) {
            throw new NotFoundException("Grades not found");
        }

        return SolutionGradeDto.builder()
                .solutionId(solutionId)
                .teamGrade(solution.getTeamGrade())
                .grades(grades.stream()
                        .map(this::mapToStudentGradeDto)
                        .toList())
                .build();
    }


    @Override
    @Transactional
    public CriterionGradeResult upsertCriterionGrade(UUID solutionId, CriterionGradeUpsertRequest request) {
        User user = meService.getMe();

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может выставлять оценку по критериям");
        }
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Список оценок по критериям обязателен");
        }

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено"));
        TaskEntity task = solution.getPost().getTask();

        if (task == null || !Boolean.TRUE.equals(task.getGradingEnabled())) {
            throw new BadRequestException("Оценивание по критериям для задания не включено");
        }

        List<CriterionEntity> criteria = criterionRepository.findAllByTask_Post_IdOrderByDisplayOrderAsc(solution.getPost().getId());
        if (criteria.isEmpty()) {
            throw new BadRequestException("У задания нет критериев оценивания");
        }

        Map<UUID, CriterionEntity> criteriaById = criteria.stream()
                .collect(Collectors.toMap(CriterionEntity::getId, c -> c));

        UUID gradedStudentId = solution.getStudentId();
        GradeEntity grade = gradeRepository.findBySolution_IdAndStudentId(solutionId, gradedStudentId)
                .orElseGet(() -> GradeEntity.builder()
                        .solution(solution)
                        .studentId(gradedStudentId)
                        .build());

        Map<UUID, CriterionGradeItemEntity> existingItems =
                grade.getCriterionItems().stream()
                        .collect(Collectors.toMap(
                                item -> item.getCriterion().getId(),
                                item -> item
                        ));

        BigDecimal regularScore = BigDecimal.ZERO;
        BigDecimal bonusScore = BigDecimal.ZERO;

        for (CriterionGradeItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getCriterionId() == null || itemRequest.getValueType() == null) {
                throw new BadRequestException("criterionId и valueType обязательны");
            }
            CriterionEntity criterion = criteriaById.get(itemRequest.getCriterionId());
            if (criterion == null) {
                throw new BadRequestException("Критерий не относится к заданию: " + itemRequest.getCriterionId());
            }
            BigDecimal calculatedScore = calculateScore(
                    criterion,
                    itemRequest.getValueType(),
                    itemRequest.getPointsValue(),
                    itemRequest.getBooleanValue(),
                    itemRequest.getPercentValue()
            );

            CriterionGradeItemEntity item = existingItems.get(criterion.getId());

            if (item == null) {
                item = new CriterionGradeItemEntity();
                item.setGrade(grade);
                item.setCriterion(criterion);

                grade.getCriterionItems().add(item);
            }

            item.setValueType(itemRequest.getValueType());
            item.setPointsValue(itemRequest.getPointsValue());
            item.setBooleanValue(itemRequest.getBooleanValue());
            item.setPercentValue(itemRequest.getPercentValue());
            item.setCalculatedScore(calculatedScore);
            item.setTeacherComment(itemRequest.getTeacherComment());

            if (criterion.getImpactType() == CriterionImpactType.BONUS) {
                bonusScore = bonusScore.add(calculatedScore);
            } else {
                regularScore = regularScore.add(calculatedScore);
            }
        }

        Integer lateDays = calculateLateDays(solution, task);
        BigDecimal latePenalty = Boolean.TRUE.equals(task.getLatePenaltyEnabled())
                ? safe(task.getLatePenaltyPerDay()).multiply(BigDecimal.valueOf(lateDays))
                : BigDecimal.ZERO;

        Integer progressMissesCount = Boolean.TRUE.equals(task.getProgressPenaltyEnabled())
                ? Optional.ofNullable(request.getProgressMissesCount()).orElse(0)
                : 0;
        if (progressMissesCount < 0) {
            throw new BadRequestException("Количество непоказов прогресса не может быть отрицательным");
        }
        BigDecimal progressPenalty = Boolean.TRUE.equals(task.getProgressPenaltyEnabled())
                ? safe(task.getProgressPenaltyPerMiss()).multiply(BigDecimal.valueOf(progressMissesCount))
                : BigDecimal.ZERO;

        BigDecimal rawScore = regularScore.add(bonusScore).subtract(latePenalty).subtract(progressPenalty);
        BigDecimal finalScore = rawScore.max(BigDecimal.ZERO).min(safe(task.getMaxFinalScore()));

        grade.setScore(finalScore.setScale(0, RoundingMode.HALF_UP).intValue());
        grade.setMaxFinalScore(task.getMaxFinalScore());
        grade.setRegularScore(regularScore);
        grade.setBonusScore(bonusScore);
        grade.setLateDays(lateDays);
        grade.setLatePenalty(latePenalty);
        grade.setProgressMissesCount(progressMissesCount);
        grade.setProgressPenalty(progressPenalty);
        grade.setRawScore(rawScore);
        grade.setFinalScore(finalScore);
        grade.setTeacherId(user.getId());
        grade.setSource(GradeSource.TEACHER);
        grade.setGradedAt(LocalDateTime.now());

        GradeEntity saved = gradeRepository.save(grade);
        return mapToCriterionGradeResult(saved);
    }

    @Override
    public CriterionGradeResult getCriterionGrade( UUID solutionId) {
        User user = meService.getMe();

        if (user.getRole() != Role.STUDENT && user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Доступ запрещен");
        }

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено"));

        if (user.getRole() == Role.STUDENT && !solution.getStudentId().equals(user.getId())) {
            throw new ForbiddenException("Доступ запрещен");
        }

        GradeEntity grade = gradeRepository.findBySolution_IdAndStudentId(solutionId, solution.getStudentId())
                .orElseThrow(() -> new NotFoundException("Оценка по критериям не найдена"));

        return mapToCriterionGradeResult(grade);
    }

    @Override
    public MemberGradeDto upsertMemberGrade(UUID solutionId, UUID studentId, GradeUpsertRequest request) {

        User user = meService.getMe();

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
        grade.setSource(GradeSource.TEACHER);
        grade.setGradedAt(LocalDateTime.now());

        GradeEntity savedGrade = gradeRepository.save(grade);

        return mapToMemberGradeDto(savedGrade);
    }

    @Override
    public MemberGradeDto getMemberGrade(UUID solutionId, UUID studentId) {

        User user = meService.getMe();

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
    public StudentPerformanceDto getStudentPerformance(UUID studentId) {

        User user = meService.getMe();

        List<GradeEntity> grades = gradeRepository.findAllByStudentId(studentId);

        if (grades.isEmpty()) {
            throw new NotFoundException("Оценки для студента не найдены");
        }

        double averageScore = grades.stream()
                .mapToInt(GradeEntity::getScore)
                .average()
                .orElse(0.0);

        return StudentPerformanceDto.builder()
                .studentId(studentId)
                .grades(grades.stream()
                        .map(this::mapToStudentPerformanceItemDto)
                        .toList())
                .averageScore(averageScore)
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
                .source(grade.getSource())
                .peerAverageScore(grade.getPeerAverageScore())
                .peerReviewsCount(grade.getPeerReviewsCount())
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


    private CriterionGradeResult mapToCriterionGradeResult(GradeEntity grade) {
        SolutionEntity solution = grade.getSolution();
        Map<UUID, SelfAssessmentItemEntity> selfAssessmentByCriterionId = selfAssessmentItemRepository.findAllBySolution_Id(solution.getId())
                .stream()
                .collect(Collectors.toMap(item -> item.getCriterion().getId(), item -> item));

        List<CriterionGradeResultItem> items = grade.getCriterionItems().stream()
                .sorted(Comparator.comparing(item -> item.getCriterion().getDisplayOrder()))
                .map(item -> CriterionGradeResultItem.builder()
                        .criterion(mapToCriterionDto(item.getCriterion()))
                        .selfAssessment(mapToSelfAssessmentDto(selfAssessmentByCriterionId.get(item.getCriterion().getId())))
                        .teacherAssessment(mapToTeacherAssessmentDto(item))
                        .build())
                .toList();

        return CriterionGradeResult.builder()
                .solutionId(solution.getId())
                .postId(solution.getPost().getId())
                .maxFinalScore(grade.getMaxFinalScore())
                .regularScore(grade.getRegularScore())
                .bonusScore(grade.getBonusScore())
                .lateDays(grade.getLateDays())
                .latePenalty(grade.getLatePenalty())
                .progressMissesCount(grade.getProgressMissesCount())
                .progressPenalty(grade.getProgressPenalty())
                .rawScore(grade.getRawScore())
                .finalScore(grade.getFinalScore())
                .gradedAt(grade.getGradedAt())
                .teacherId(grade.getTeacherId())
                .items(items)
                .build();
    }

    private CriterionDto mapToCriterionDto(CriterionEntity criterion) {
        return CriterionDto.builder()
                .id(criterion.getId())
                .postId(criterion.getTask().getPost().getId())
                .title(criterion.getTitle())
                .description(criterion.getDescription())
                .type(criterion.getType())
                .maxScore(criterion.getMaxScore())
                .impactType(criterion.getImpactType())
                .displayOrder(criterion.getDisplayOrder())
                .build();
    }

    private SelfAssessmentItemDto mapToSelfAssessmentDto(SelfAssessmentItemEntity item) {
        if (item == null) {
            return null;
        }
        return SelfAssessmentItemDto.builder()
                .criterionId(item.getCriterion().getId())
                .valueType(item.getValueType())
                .pointsValue(item.getPointsValue())
                .booleanValue(item.getBooleanValue())
                .percentValue(item.getPercentValue())
                .calculatedScore(item.getCalculatedScore())
                .comment(item.getComment())
                .build();
    }

    private TeacherAssessmentItemDto mapToTeacherAssessmentDto(CriterionGradeItemEntity item) {
        return TeacherAssessmentItemDto.builder()
                .criterionId(item.getCriterion().getId())
                .valueType(item.getValueType())
                .pointsValue(item.getPointsValue())
                .booleanValue(item.getBooleanValue())
                .percentValue(item.getPercentValue())
                .calculatedScore(item.getCalculatedScore())
                .teacherComment(item.getTeacherComment())
                .build();
    }

    private Integer calculateLateDays(SolutionEntity solution, TaskEntity task) {
        if (!Boolean.TRUE.equals(task.getLatePenaltyEnabled()) || task.getDeadline() == null || solution.getSubmittedAt() == null) {
            return 0;
        }
        if (!solution.getSubmittedAt().isAfter(task.getDeadline())) {
            return 0;
        }
        long minutes = ChronoUnit.MINUTES.between(task.getDeadline(), solution.getSubmittedAt());
        return (int) Math.ceil(minutes / (60.0 * 24.0));
    }

    private BigDecimal calculateScore(CriterionEntity criterion,
                                      CriterionValueType valueType,
                                      BigDecimal pointsValue,
                                      Boolean booleanValue,
                                      BigDecimal percentValue) {
        return switch (valueType) {
            case POINTS -> {
                if (pointsValue == null || pointsValue.compareTo(BigDecimal.ZERO) < 0 || pointsValue.compareTo(criterion.getMaxScore()) > 0) {
                    throw new BadRequestException("Баллы должны быть от 0 до максимума критерия");
                }
                yield pointsValue;
            }
            case YES_NO -> {
                if (booleanValue == null) {
                    throw new BadRequestException("Для YES_NO нужно указать booleanValue");
                }
                yield Boolean.TRUE.equals(booleanValue) ? criterion.getMaxScore() : BigDecimal.ZERO;
            }
            case PERCENT -> {
                if (percentValue == null || percentValue.compareTo(BigDecimal.ZERO) < 0 || percentValue.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new BadRequestException("Процент должен быть от 0 до 100");
                }
                yield criterion.getMaxScore().multiply(percentValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        };
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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
