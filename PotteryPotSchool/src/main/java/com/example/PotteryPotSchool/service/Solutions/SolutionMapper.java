package com.example.PotteryPotSchool.service.Solutions;

import com.example.PotteryPotSchool.dto.Solutions.*;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.repository.SolutionVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolutionMapper {

    private final SolutionVoteRepository voteRepository;

    public Solution toDto(SolutionEntity e) {
        return Solution.builder()
                .id(e.getId())
                .postId(e.getPost().getId())
                .ownerType(e.getOwnerType())
                .studentId(e.getStudentId())
                .teamId(e.getTeamId())
                .status(e.getStatus())
                .text(e.getText())
                .videoUrl(e.getVideoUrl())
                .attachmentUrl(e.getAttachmentUrl())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .submittedAt(e.getSubmittedAt())
                .authorStudentId(e.getStudentId())
                .votesCount((int) voteRepository.countBySolutionId(e.getId()))
                .build();
    }

    public SolutionSummaryDto toSummaryDto(SolutionEntity e) {
        return SolutionSummaryDto.builder()
                .id(e.getId())
                .postId(e.getPost().getId())
                .ownerType(e.getOwnerType())
                .studentId(e.getStudentId())
                .teamId(e.getTeamId())
                .status(e.getStatus())
                .submittedAt(e.getSubmittedAt())
                .authorStudentId(e.getStudentId())
                .votesCount((int) voteRepository.countBySolutionId(e.getId()))
                .build();
    }

    public SolutionDetailsDto toDetailsDto(SolutionEntity e) {
        return SolutionDetailsDto.builder()
                .id(e.getId())
                .postId(e.getPost().getId())
                .ownerType(e.getOwnerType())
                .studentId(e.getStudentId())
                .teamId(e.getTeamId())
                .status(e.getStatus())
                .text(e.getText())
                .videoUrl(e.getVideoUrl())
                .attachmentUrl(e.getAttachmentUrl())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .submittedAt(e.getSubmittedAt())
                .authorStudentId(e.getStudentId())
                .votesCount((int) voteRepository.countBySolutionId(e.getId()))
                .build();
    }
}