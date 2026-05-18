package com.example.PotteryPotSchool.controller;

import com.example.PotteryPotSchool.dto.Criteria.CriterionCreateRequest;
import com.example.PotteryPotSchool.dto.Criteria.CriterionDto;
import com.example.PotteryPotSchool.dto.Criteria.CriterionUpdateRequest;
import com.example.PotteryPotSchool.service.Criteria.CriterionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CriteriaController {

    private final CriterionService criterionService;

    @GetMapping("/posts/{postId}/criteria")
    public List<CriterionDto> getCriteria(@PathVariable UUID postId) {
        return criterionService.getByPostId(postId);
    }

    @PostMapping("/posts/{postId}/criteria")
    @ResponseStatus(HttpStatus.CREATED)
    public CriterionDto createCriterion(@PathVariable UUID postId,
                                        @RequestBody CriterionCreateRequest request) {
        return criterionService.create(postId, request);
    }

    @PatchMapping("/criteria/{criterionId}")
    public CriterionDto updateCriterion(@PathVariable UUID criterionId,
                                        @RequestBody CriterionUpdateRequest request) {
        return criterionService.update(criterionId, request);
    }

    @DeleteMapping("/criteria/{criterionId}")
    public ResponseEntity<Void> deleteCriterion(@PathVariable UUID criterionId) {
        criterionService.delete(criterionId);
        return ResponseEntity.noContent().build();
    }
}
