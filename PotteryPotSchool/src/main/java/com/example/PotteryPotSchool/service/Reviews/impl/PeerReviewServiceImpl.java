package com.example.PotteryPotSchool.service.Reviews.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Reviews.PeerReviewAssignmentDto;
import com.example.PotteryPotSchool.dto.Reviews.PeerReviewDto;
import com.example.PotteryPotSchool.dto.Reviews.PeerReviewUpsertRequest;
import com.example.PotteryPotSchool.dto.Solutions.Solution;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.entity.Reviews.PeerReviewEntity;
import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Grades.GradeSource;
import com.example.PotteryPotSchool.enums.Posts.ReviewType;
import com.example.PotteryPotSchool.enums.Solutions.PeerReviewStatus;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.GradeRepository;
import com.example.PotteryPotSchool.repository.PeerReviewRepository;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Reviews.PeerReviewService;
import com.example.PotteryPotSchool.service.Solutions.SolutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PeerReviewServiceImpl implements PeerReviewService {

    private final PeerReviewRepository peerReviewRepository;
    private final SolutionRepository solutionRepository;
    private final GradeRepository gradeRepository;
    private final SolutionMapper solutionMapper;
    private final MeService meService;


    @Override
    @Transactional
    public List<PeerReviewDto> assign(UUID postId) {
        User user = meService.getMe();
        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может распределять работы на проверку");
        }

        List<SolutionEntity> submitted = solutionRepository.findByPostIdAndStatus(postId, SolutionStatus.SUBMITTED);
        if (submitted.isEmpty()) {
            throw new BadRequestException("Нет отправленных решений для распределения");
        }

        TaskEntity task = submitted.get(0).getPost().getTask();
        if (task == null || task.getReviewType() != ReviewType.PEER_TO_PEER) {
            throw new BadRequestException("Для задания не включена проверка peer-to-peer");
        }

        int perStudent = task.getReviewsPerStudent() == null ? 0 : task.getReviewsPerStudent();
        if (perStudent < 1) {
            throw new BadRequestException("Не задано количество работ для проверки");
        }
        if (submitted.size() < perStudent + 1) {
            throw new BadRequestException("Недостаточно решений: каждому студенту нужно проверить "
                    + perStudent + " чужих работ");
        }

        peerReviewRepository.deleteAllByPostId(postId);

        LocalDateTime now = LocalDateTime.now();
        int n = submitted.size();
        List<PeerReviewEntity> assignments = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            SolutionEntity reviewerSolution = submitted.get(i);
            UUID reviewerId = reviewerSolution.getStudentId();
            if (reviewerId == null) {
                continue;
            }
            int assigned = 0;
            int offset = 1;
            while (assigned < perStudent && offset < n) {
                SolutionEntity target = submitted.get((i + offset) % n);
                offset++;
                if (reviewerId.equals(target.getStudentId())) {
                    continue;
                }
                assignments.add(PeerReviewEntity.builder()
                        .solution(target)
                        .postId(postId)
                        .reviewerId(reviewerId)
                        .status(PeerReviewStatus.ASSIGNED)
                        .createdAt(now)
                        .build());
                assigned++;
            }
        }

        return peerReviewRepository.saveAll(assignments).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeerReviewAssignmentDto> getMyAssignments(UUID postId) {
        User user = meService.getMe();
        if (user.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Назначенные проверки доступны только студенту");
        }

        return peerReviewRepository.findAllByPostIdAndReviewerId(postId, user.getId()).stream()
                .map(review -> {
                    TaskEntity task = review.getSolution().getPost().getTask();
                    return PeerReviewAssignmentDto.builder()
                            .review(toDto(review))
                            .solution(anonymize(solutionMapper.toDto(review.getSolution())))
                            .reviewDeadline(task != null ? task.getReviewDeadline() : null)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public PeerReviewDto upsertReview(UUID solutionId, PeerReviewUpsertRequest request) {
        User user = meService.getMe();
        if (user.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Проверять работы могут только студенты");
        }
        if (request == null || request.getScore() == null) {
            throw new BadRequestException("Оценка обязательна");
        }
        if (request.getScore() < 1 || request.getScore() > 5) {
            throw new BadRequestException("Оценка должна быть от 1 до 5");
        }

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено"));

        TaskEntity task = solution.getPost().getTask();
        if (task == null || task.getReviewType() != ReviewType.PEER_TO_PEER) {
            throw new BadRequestException("Для задания не включена проверка peer-to-peer");
        }
        if (task.getReviewDeadline() != null && LocalDateTime.now().isAfter(task.getReviewDeadline())) {
            throw new ForbiddenException("Дедлайн проверки прошёл");
        }

        PeerReviewEntity review = peerReviewRepository
                .findBySolution_IdAndReviewerId(solutionId, user.getId())
                .orElseThrow(() -> new ForbiddenException("Эта работа не назначена вам на проверку"));

        review.setScore(request.getScore());
        review.setComment(request.getComment());
        review.setStatus(PeerReviewStatus.SUBMITTED);
        review.setSubmittedAt(LocalDateTime.now());

        PeerReviewEntity saved = peerReviewRepository.save(review);

        recomputeSolutionGrade(solution);

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PeerReviewDto getMyReview(UUID solutionId) {
        User user = meService.getMe();
        if (user.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Доступ запрещён");
        }
        PeerReviewEntity review = peerReviewRepository
                .findBySolution_IdAndReviewerId(solutionId, user.getId())
                .orElseThrow(() -> new NotFoundException("Проверка не найдена"));
        return toDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeerReviewDto> getReviewsForSolution(UUID solutionId) {
        User user = meService.getMe();
        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Все проверки решения доступны только преподавателю");
        }
        solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено"));
        return peerReviewRepository.findAllBySolution_Id(solutionId).stream()
                .map(this::toDto)
                .toList();
    }

    private void recomputeSolutionGrade(SolutionEntity solution) {
        UUID authorId = solution.getStudentId();
        if (authorId == null) {
            return;
        }

        List<PeerReviewEntity> submittedReviews = peerReviewRepository
                .findAllBySolution_IdAndStatus(solution.getId(), PeerReviewStatus.SUBMITTED);
        if (submittedReviews.isEmpty()) {
            return;
        }

        double average = submittedReviews.stream()
                .mapToInt(PeerReviewEntity::getScore)
                .average()
                .orElse(0.0);
        BigDecimal peerAverage = BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
        int peerCount = submittedReviews.size();

        GradeEntity grade = gradeRepository
                .findBySolution_IdAndStudentId(solution.getId(), authorId)
                .orElseGet(() -> GradeEntity.builder()
                        .solution(solution)
                        .studentId(authorId)
                        .source(GradeSource.PEER_AVERAGE)
                        .build());

        grade.setPeerAverageScore(peerAverage);
        grade.setPeerReviewsCount(peerCount);

        if (grade.getSource() != GradeSource.TEACHER) {
            grade.setSource(GradeSource.PEER_AVERAGE);
            grade.setScore(peerAverage.setScale(0, RoundingMode.HALF_UP).intValue());
            grade.setFinalScore(peerAverage);
            grade.setTeacherId(null);
            grade.setGradedAt(LocalDateTime.now());
        }

        gradeRepository.save(grade);
    }

    private PeerReviewDto toDto(PeerReviewEntity e) {
        return PeerReviewDto.builder()
                .id(e.getId())
                .solutionId(e.getSolution().getId())
                .postId(e.getPostId())
                .reviewerId(e.getReviewerId())
                .reviewerName(e.getReviewerName())
                .status(e.getStatus())
                .score(e.getScore())
                .comment(e.getComment())
                .createdAt(e.getCreatedAt())
                .submittedAt(e.getSubmittedAt())
                .build();
    }

    private Solution anonymize(Solution s) {
        s.setStudentId(null);
        s.setStudentName(null);
        s.setAuthorStudentId(null);
        return s;
    }
}
