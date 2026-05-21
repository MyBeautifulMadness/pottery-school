package com.example.PotteryPotSchool.service.Solutions.impl;

import com.example.PotteryPotSchool.config.*;
import com.example.PotteryPotSchool.dto.Profiles.Profile;
import com.example.PotteryPotSchool.dto.Grades.SelfAssessmentItemRequest;
import com.example.PotteryPotSchool.dto.Solutions.*;
import com.example.PotteryPotSchool.dto.Teams.Team;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Grades.CriterionEntity;
import com.example.PotteryPotSchool.entity.Grades.SelfAssessmentItemEntity;
import com.example.PotteryPotSchool.entity.Posts.TaskEntity;
import com.example.PotteryPotSchool.entity.Solutions.*;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Posts.PrioritySolution;
import com.example.PotteryPotSchool.enums.Posts.TaskMode;
import com.example.PotteryPotSchool.enums.Solutions.*;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.*;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.service.Login.JwtService;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.Solutions.*;
import com.example.PotteryPotSchool.service.Teams.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolutionServiceImpl implements SolutionService {

    private final SolutionRepository solutionRepository;
    private final SolutionVoteRepository voteRepository;
    private final PostRepository postRepository;
    private final MeService meService;
    private final SolutionMapper solutionMapper;
    private final JwtService jwtService;
    private final TeamService teamService;
    private final CriterionRepository criterionRepository;
    private final SelfAssessmentItemRepository selfAssessmentItemRepository;

    @Override
    @Transactional
    public Solution create(UUID postId, SolutionCreateRequest request) {

        User user = meService.getMe();
        ensureStudent(user);

        Profile profile = meService.getMyProfile();

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (post.getType() != PostType.TASK) {
            throw new BadRequestException("Only TASK");
        }

        TaskEntity task = post.getTask();
        boolean isTeam = task.getMode() == TaskMode.TEAM;

        if (isTeam) {
            UUID myTeamId = teamService.getMyTeam(postId).getId();
            if (!myTeamId.equals(request.getTeamId())) {
                throw new ForbiddenException("Wrong team");
            }
        } else {
            if (solutionRepository.existsByPostIdAndStudentId(postId, user.getId())) {
                throw new BadRequestException("Already exists");
            }
        }

        LocalDateTime now = LocalDateTime.now();

        SolutionEntity solution = SolutionEntity.builder()
                .post(post)
                .studentId(user.getId())
                .studentName(profile.getFullName())
                .teamId(request.getTeamId())
                .ownerType(isTeam ? SolutionOwnerType.TEAM : SolutionOwnerType.STUDENT)
                .status(SolutionStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();

        apply(solution, request);

        if (request.getSelfAssessment() != null) {
            applySelfAssessment(solution, task, request.getSelfAssessment());
        }

        if (Boolean.TRUE.equals(request.getSubmit())) {
            validateSelfAssessmentForSubmit(solution, task);
            solution.setStatus(SolutionStatus.SUBMITTED);
            solution.setSubmittedAt(now);
        }

        return solutionMapper.toDto(solutionRepository.save(solution));
    }

    @Override
    public Solution getMySolution(UUID postId, UUID studentId) {
        Optional<SolutionEntity> solution = solutionRepository.findByPostIdAndStudentId(postId, studentId);
        if(solution.isPresent()){
            return solutionMapper.toDto(solution.get());
        }
        else return null;
    }

    public List<Solution> getTeamSolutions(UUID postId, UUID studentId) {

        Team team = teamService.getMyTeam(postId);

        return solutionRepository.findByPostId(postId)
                .stream()
                .filter(s -> team.getMembers().stream()
                        .anyMatch(m -> m.getId().equals(s.getStudentId())))
                .map(solutionMapper::toDto)
                .toList();
    }

    @Override
    public Solution getSelected(UUID postId) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        User user = meService.getMe();
        ensureStudent(user);

        if (user.getRole()!=Role.STUDENT) {
            throw new ForbiddenException("Forbidden");
        }


        PrioritySolution priority;
        Team team;
        if(post.getTask().getPrioritySolution()!= null){
            team = teamService.getMyTeam(postId);
            priority = post.getTask().getPrioritySolution();
        }

        else {priority=null; team = null;}

        List<SolutionEntity> solutions = solutionRepository.findByPostIdAndStatus(
                postId, SolutionStatus.SUBMITTED
        );

        SolutionEntity result;

        switch (priority) {

            case CAPITAIN -> {

                if (team.getCaptainId() == null) {
                    result = null;
                    break;
                }

                UUID captainId = team.getCaptainId();

                result = solutions.stream()
                        .filter(s -> captainId.equals(s.getStudentId()))
                        .findFirst()
                        .orElse(null);
            }

            case FIRST -> result = solutions.stream()
                    .min(Comparator.comparing(SolutionEntity::getSubmittedAt))
                    .orElse(null);

            case LAST -> result = solutions.stream()
                    .max(Comparator.comparing(SolutionEntity::getSubmittedAt))
                    .orElse(null);

            case VOTING -> result = solutions.stream()
                    .max(Comparator.comparing(s ->
                            voteRepository.countBySolutionId(s.getId())
                    ))
                    .orElse(null);
            case null -> result = solutions.stream()
                    .min(Comparator.comparing(SolutionEntity::getSubmittedAt))
                    .orElse(null);

            default -> result = null;
        }

        if (result == null) {
            throw new NotFoundException("Not found");
        }

        return solutionMapper.toDto(result);
    }

    @Override
    @Transactional
    public Solution update(UUID solutionId, SolutionUpdateRequest request) {

        User user = meService.getMe();
        ensureStudent(user);

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        if (!solution.getStudentId().equals(user.getId())) {
            throw new ForbiddenException("Forbidden");
        }

        apply(solution, request);
        if (request.getSelfAssessment() != null) {
            selfAssessmentItemRepository.deleteBySolution_Id(solution.getId());
            selfAssessmentItemRepository.flush();

            applySelfAssessment(solution, solution.getPost().getTask(), request.getSelfAssessment());
        }
        solution.setUpdatedAt(LocalDateTime.now());

        return solutionMapper.toDto(solutionRepository.save(solution));
    }

    @Override
    @Transactional
    public Solution submit(UUID solutionId) {

        User user = meService.getMe();
        ensureStudent(user);

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        if (!solution.getStudentId().equals(user.getId())) {
            throw new ForbiddenException("Forbidden");
        }

        LocalDateTime now = LocalDateTime.now();
        validateSelfAssessmentForSubmit(solution, solution.getPost().getTask());
        solution.setStatus(SolutionStatus.SUBMITTED);
        solution.setSubmittedAt(now);
        solution.setUpdatedAt(now);

        return solutionMapper.toDto(solutionRepository.save(solution));
    }

    @Override
    public SolutionDetailsDto getSolution(UserPrincipal user, UUID solutionId) {

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        if (user.getRole() == Role.STUDENT &&
                !solution.getStudentId().equals(user.getId())) {
            throw new ForbiddenException("Forbidden");
        }

        return solutionMapper.toDetailsDto(solution);
    }

    @Override
    public List<SolutionSummaryDto> getSolutions(
            UUID postId,
            SolutionStatus status,
            SolutionOwnerType ownerType,
            UUID teamId,
            UUID studentId,
            UUID authorStudentId,
            Boolean selectedOnly,
            UserPrincipal user
    ) {

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only teacher");
        }

        Set<UUID> selectedIds = null;

        if (Boolean.TRUE.equals(selectedOnly)) {

            selectedIds = teamService.getTeamsByPostId(postId)
                    .stream()
                    .map(team -> findSelectedSolutionEntity(postId, team.getId()))
                    .filter(Objects::nonNull)
                    .map(SolutionEntity::getId)
                    .collect(Collectors.toSet());

            SolutionEntity soloSelected = findSelectedSolutionEntity(postId, null);
            if (soloSelected != null) {
                selectedIds.add(soloSelected.getId());
            }

            if (selectedIds.isEmpty()) {
                return Collections.emptyList();
            }
        }

        Set<UUID> finalSelectedIds = selectedIds;

        return solutionRepository.findByPostId(postId)
                .stream()
                .filter(s -> status == null || s.getStatus() == status)
                .filter(s -> ownerType == null || s.getOwnerType() == ownerType)
                .filter(s -> teamId == null || teamId.equals(s.getTeamId()))
                .filter(s -> studentId == null || studentId.equals(s.getStudentId()))
                .filter(s -> authorStudentId == null || authorStudentId.equals(s.getStudentId()))
                .filter(s -> finalSelectedIds == null || finalSelectedIds.contains(s.getId()))
                .map(solutionMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional
    public Solution vote(UUID solutionId) {

        User user = meService.getMe();

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        UUID postId = solution.getPost().getId();

        voteRepository.deleteByPostIdAndStudentId(postId, user.getId());

        SolutionVote vote = SolutionVote.builder()
                .solutionId(solutionId)
                .studentId(user.getId())
                .postId(postId)
                .build();

        voteRepository.save(vote);

        return solutionMapper.toDto(solution);
    }

    @Override
    @Transactional
    public Solution unvote(UUID solutionId) {

        User user = meService.getMe();

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        voteRepository.deleteByPostIdAndStudentId(
                solution.getPost().getId(),
                user.getId()
        );

        return solutionMapper.toDto(solution);
    }

    @Override
    public Solution unsubmit(UUID solutionId) {

        User user = meService.getMe();
        ensureStudent(user);

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Not found"));

        if (!solution.getStudentId().equals(user.getId())) {
            throw new ForbiddenException("Forbidden");
        }

        LocalDateTime now = LocalDateTime.now();
        solution.setStatus(SolutionStatus.DRAFT);
        solution.setSubmittedAt(null);
        solution.setUpdatedAt(now);

        return solutionMapper.toDto(solutionRepository.save(solution));
    }


    private void applySelfAssessment(SolutionEntity solution, TaskEntity task, List<SelfAssessmentItemRequest> items) {
        solution.getSelfAssessmentItems().clear();
        if (items == null || items.isEmpty()) {
            return;
        }

        Map<UUID, CriterionEntity> criteriaById = criterionRepository.findAllByTask_Post_IdOrderByDisplayOrderAsc(task.getPost().getId())
                .stream()
                .collect(Collectors.toMap(CriterionEntity::getId, c -> c));

        Set<UUID> usedCriterionIds = new HashSet<>();
        for (SelfAssessmentItemRequest item : items) {
            if (item.getCriterionId() == null || item.getValueType() == null) {
                throw new BadRequestException("criterionId и valueType обязательны для самооценки");
            }
            if (!usedCriterionIds.add(item.getCriterionId())) {
                throw new BadRequestException("Самооценка содержит повторяющийся criterionId");
            }
            CriterionEntity criterion = criteriaById.get(item.getCriterionId());
            if (criterion == null) {
                throw new BadRequestException("Критерий не относится к этому заданию: " + item.getCriterionId());
            }
            BigDecimal calculatedScore = calculateScore(criterion, item.getValueType(), item.getPointsValue(), item.getBooleanValue(), item.getPercentValue());
            solution.getSelfAssessmentItems().add(SelfAssessmentItemEntity.builder()
                    .solution(solution)
                    .criterion(criterion)
                    .valueType(item.getValueType())
                    .pointsValue(item.getPointsValue())
                    .booleanValue(item.getBooleanValue())
                    .percentValue(item.getPercentValue())
                    .calculatedScore(calculatedScore)
                    .comment(item.getComment())
                    .build());
        }
    }

    private void validateSelfAssessmentForSubmit(SolutionEntity solution, TaskEntity task) {
        if (task == null || !Boolean.TRUE.equals(task.getGradingEnabled()) || !Boolean.TRUE.equals(task.getSelfAssessmentRequired())) {
            return;
        }

        List<CriterionEntity> criteria = criterionRepository.findAllByTask_Post_IdOrderByDisplayOrderAsc(task.getPost().getId());
        Set<UUID> assessedIds = solution.getSelfAssessmentItems() == null ? Set.of() : solution.getSelfAssessmentItems().stream()
                .map(item -> item.getCriterion().getId())
                .collect(Collectors.toSet());

        if (criteria.isEmpty() || !assessedIds.containsAll(criteria.stream().map(CriterionEntity::getId).collect(Collectors.toSet()))) {
            throw new BadRequestException("Для сдачи работы нужно заполнить самооценку по всем критериям");
        }
    }

    private BigDecimal calculateScore(CriterionEntity criterion,
                                      com.example.PotteryPotSchool.enums.Grades.CriterionValueType valueType,
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

    private void apply(SolutionEntity s, SolutionCreateRequest r) {
        s.setText(r.getText());
        s.setVideoUrl(r.getVideoUrl());
        s.setAttachmentUrl(r.getAttachmentUrl());
    }

    private void apply(SolutionEntity s, SolutionUpdateRequest r) {
        s.setText(r.getText());
        s.setVideoUrl(r.getVideoUrl());
        s.setAttachmentUrl(r.getAttachmentUrl());
    }

    private void ensureStudent(User user) {
        if (user.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only student");
        }
    }


    private SolutionEntity findSelectedSolutionEntity(UUID postId, UUID teamId) {

        PostEntity post = postRepository.findById(postId)
                .orElse(null);

        if (post == null) {
            return null;
        }

        PrioritySolution priority = post.getTask().getPrioritySolution();
        Team team = null;

        if (priority != null && teamId != null) {
            team = teamService.getTeamById(postId, teamId);
        }

        List<SolutionEntity> solutions = solutionRepository
                .findByPostIdAndStatus(postId, SolutionStatus.SUBMITTED)
                .stream()
                .filter(s -> {
                    if (teamId == null) {
                        return s.getTeamId() == null; // solo
                    }
                    return teamId.equals(s.getTeamId());
                })
                .toList();

        if (solutions.isEmpty()) {
            return null;
        }

        return switch (priority) {

            case CAPITAIN -> {
                if (team == null || team.getCaptainId() == null) {
                    yield null;
                }

                UUID captainId = team.getCaptainId();

                yield solutions.stream()
                        .filter(s -> captainId.equals(s.getStudentId()))
                        .findFirst()
                        .orElse(null);
            }

            case FIRST -> solutions.stream()
                    .min(Comparator.comparing(SolutionEntity::getSubmittedAt))
                    .orElse(null);

            case LAST -> solutions.stream()
                    .max(Comparator.comparing(SolutionEntity::getSubmittedAt))
                    .orElse(null);

            case VOTING -> solutions.stream()
                    .max(Comparator.comparing(s ->
                            voteRepository.countBySolutionId(s.getId())
                    ))
                    .orElse(null);

            case null -> solutions.stream()
                    .min(Comparator.comparing(SolutionEntity::getSubmittedAt))
                    .orElse(null);

            default -> null;
        };
    }
}