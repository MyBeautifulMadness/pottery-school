package com.example.PotteryPotSchool.service.Solutions.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Solutions.SolutionDto;
import com.example.PotteryPotSchool.dto.Solutions.SolutionSummaryDto;
import com.example.PotteryPotSchool.dto.Solutions.Solution;
import com.example.PotteryPotSchool.dto.Solutions.SolutionUpsertRequest;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.repository.SolutionRepository;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.security.UserPrincipal;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.service.Solutions.SolutionMapper;
import com.example.PotteryPotSchool.service.Solutions.SolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolutionServiceImpl implements SolutionService {

    private final SolutionRepository solutionRepository;
    private final SolutionMapper solutionMapper;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MeService meService;

    @Override
    public Solution createOrUpdate(UUID postId, SolutionUpsertRequest request) {
        User currentUser = meService.getMe();
        ensureStudent(currentUser);

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Пост не найден: " + postId));

        if (post.getType() != PostType.TASK) {
            throw new BadRequestException("Решение можно прикрепить только к посту типа TASK");
        }

        UserEntity student = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + currentUser.getId()));

        boolean submit = Boolean.TRUE.equals(request.getSubmit());
        LocalDateTime now = LocalDateTime.now();

        SolutionEntity solution = solutionRepository.findByPostIdAndStudentId(postId, currentUser.getId())
                .orElseGet(() -> SolutionEntity.builder()
                        .post(post)
                        .studentId(currentUser.getId())
                        .status(SolutionStatus.DRAFT)
                        .createdAt(now)
                        .build());

        solution.setText(request.getText());
        solution.setVideoUrl(request.getVideoUrl());
        solution.setAttachmentUrl(request.getAttachmentUrl());
        solution.setUpdatedAt(now);

        if (submit) {
            solution.setStatus(SolutionStatus.SUBMITTED);
            solution.setSubmittedAt(now);
        }

        SolutionEntity saved = solutionRepository.save(solution);
        return mapToDto(saved);
    }

    @Override
    public Solution submit(UUID solutionId) {
        User currentUser = meService.getMe();
        ensureStudent(currentUser);

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new NotFoundException("Решение не найдено: " + solutionId));

        if (!solution.getStudentId().equals(currentUser.getId())) {
            throw new ForbiddenException("Вы можете отправить только своё решение");
        }

        if (solution.getStatus() != SolutionStatus.SUBMITTED) {
            LocalDateTime now = LocalDateTime.now();
            solution.setStatus(SolutionStatus.SUBMITTED);
            solution.setSubmittedAt(now);
            solution.setUpdatedAt(now);
            solution = solutionRepository.save(solution);
        }

        return mapToDto(solution);
    }

    private void ensureStudent(User currentUser) {
        if (currentUser.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Только студенты могут отправлять решения");
        }
    }

    private Solution mapToDto(SolutionEntity entity) {
        return Solution.builder()
                .id(entity.getId())
                .postId(entity.getPost().getId())
                .studentId(entity.getStudentId())
                .status(entity.getStatus())
                .text(entity.getText())
                .videoUrl(entity.getVideoUrl())
                .attachmentUrl(entity.getAttachmentUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .submittedAt(entity.getSubmittedAt())
                .build();
    }

    @Override
    public List<SolutionSummaryDto> getSolutions(
            UUID postId,
            SolutionStatus status,
            UserPrincipal user
    ) {

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only teacher can view solutions");
        }

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (post.getType() != PostType.TASK) {
            throw new NotFoundException("Solutions exist only for TASK posts");
        }

        List<SolutionSummaryDto> solutions;

        if (status == null) {
            solutions = solutionRepository.findByPostId(postId);
        } else {
            solutions = solutionRepository.findByPostIdAndStatus(postId, status);
        }

        return solutions;
    }

    @Override
    public SolutionDto getMySolution(UUID postId, UUID studentId) {

        SolutionEntity solution = solutionRepository
                .findByPostIdAndStudentId(postId, studentId)
                .orElseThrow(() -> new NotFoundException("Solution not found"));

        return solutionMapper.toDto(solution);
    }
}
