package com.example.PotteryPotSchool.service.Solutions;

import com.example.PotteryPotSchool.dto.Solutions.SolutionDto;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import org.springframework.stereotype.Component;

@Component
public class SolutionMapper {

    public SolutionDto toDto(SolutionEntity s) {

        return SolutionDto.builder()
                .id(s.getId())
                .postId(s.getPost().getId())
                .studentId(s.getStudentId())
                .status(s.getStatus())
                .text(s.getText())
                .videoUrl(s.getVideoUrl())
                .attachmentUrl(s.getAttachmentUrl())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .submittedAt(s.getSubmittedAt())
                .build();
    }
}
