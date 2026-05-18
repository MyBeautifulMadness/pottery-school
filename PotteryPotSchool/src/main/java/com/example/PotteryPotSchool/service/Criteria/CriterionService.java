package com.example.PotteryPotSchool.service.Criteria;

import com.example.PotteryPotSchool.dto.Criteria.CriterionCreateRequest;
import com.example.PotteryPotSchool.dto.Criteria.CriterionDto;
import com.example.PotteryPotSchool.dto.Criteria.CriterionUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface CriterionService {
    List<CriterionDto> getByPostId(UUID postId);
    CriterionDto create(UUID postId, CriterionCreateRequest request);
    CriterionDto update(UUID criterionId, CriterionUpdateRequest request);
    void delete(UUID criterionId);
}
