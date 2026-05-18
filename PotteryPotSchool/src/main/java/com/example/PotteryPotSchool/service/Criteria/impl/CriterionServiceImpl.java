package com.example.PotteryPotSchool.service.Criteria.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.config.NotFoundException;
import com.example.PotteryPotSchool.dto.Criteria.CriterionCreateRequest;
import com.example.PotteryPotSchool.dto.Criteria.CriterionDto;
import com.example.PotteryPotSchool.dto.Criteria.CriterionUpdateRequest;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Grades.CriterionEntity;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.enums.Posts.PostType;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.CriterionRepository;
import com.example.PotteryPotSchool.repository.PostRepository;
import com.example.PotteryPotSchool.service.Criteria.CriterionService;
import com.example.PotteryPotSchool.service.Me.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriterionServiceImpl implements CriterionService {

    private final CriterionRepository criterionRepository;
    private final PostRepository postRepository;
    private final MeService meService;

    @Override
    public List<CriterionDto> getByPostId(UUID postId) {
        meService.getMe();
        ensureTaskPost(postId);
        return criterionRepository.findAllByTask_Post_IdOrderByDisplayOrderAsc(postId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CriterionDto create(UUID postId, CriterionCreateRequest request) {
        ensureTeacher();
        PostEntity post = ensureTaskPost(postId);
        validateCreate(request);

        CriterionEntity criterion = CriterionEntity.builder()
                .task(post.getTask())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .type(request.getType())
                .maxScore(request.getMaxScore())
                .impactType(request.getImpactType())
                .displayOrder(request.getDisplayOrder())
                .build();

        return toDto(criterionRepository.save(criterion));
    }

    @Override
    @Transactional
    public CriterionDto update(UUID criterionId, CriterionUpdateRequest request) {
        ensureTeacher();
        if (request == null) {
            throw new BadRequestException("Тело запроса обязательно");
        }
        CriterionEntity criterion = criterionRepository.findById(criterionId)
                .orElseThrow(() -> new NotFoundException("Критерий не найден"));

        if (request.getTitle() != null) {
            if (request.getTitle().trim().isEmpty()) {
                throw new BadRequestException("Название критерия не может быть пустым");
            }
            criterion.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) criterion.setDescription(request.getDescription());
        if (request.getType() != null) criterion.setType(request.getType());
        if (request.getMaxScore() != null) {
            if (request.getMaxScore().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("maxScore не может быть отрицательным");
            }
            criterion.setMaxScore(request.getMaxScore());
        }
        if (request.getImpactType() != null) criterion.setImpactType(request.getImpactType());
        if (request.getDisplayOrder() != null) {
            if (request.getDisplayOrder() < 0) {
                throw new BadRequestException("displayOrder не может быть отрицательным");
            }
            criterion.setDisplayOrder(request.getDisplayOrder());
        }

        return toDto(criterionRepository.save(criterion));
    }

    @Override
    @Transactional
    public void delete(UUID criterionId) {
        ensureTeacher();
        CriterionEntity criterion = criterionRepository.findById(criterionId)
                .orElseThrow(() -> new NotFoundException("Критерий не найден"));
        criterionRepository.delete(criterion);
    }

    private void ensureTeacher() {
        User user = meService.getMe();
        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Только преподаватель может управлять критериями");
        }
    }

    private PostEntity ensureTaskPost(UUID postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Пост не найден"));
        if (post.getType() != PostType.TASK || post.getTask() == null) {
            throw new BadRequestException("Критерии можно создавать только для TASK-поста");
        }
        return post;
    }

    private void validateCreate(CriterionCreateRequest request) {
        if (request == null) throw new BadRequestException("Тело запроса обязательно");
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Название критерия обязательно");
        }
        if (request.getType() == null) throw new BadRequestException("Тип критерия обязателен");
        if (request.getImpactType() == null) throw new BadRequestException("Тип влияния критерия обязателен");
        if (request.getMaxScore() == null || request.getMaxScore().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("maxScore обязателен и не может быть отрицательным");
        }
        if (request.getDisplayOrder() == null || request.getDisplayOrder() < 0) {
            throw new BadRequestException("displayOrder обязателен и не может быть отрицательным");
        }
    }

    private CriterionDto toDto(CriterionEntity e) {
        return CriterionDto.builder()
                .id(e.getId())
                .postId(e.getTask().getPost().getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .type(e.getType())
                .maxScore(e.getMaxScore())
                .impactType(e.getImpactType())
                .displayOrder(e.getDisplayOrder())
                .build();
    }
}
