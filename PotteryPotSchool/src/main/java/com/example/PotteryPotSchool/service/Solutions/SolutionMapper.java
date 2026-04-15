package com.example.PotteryPotSchool.service.Solutions;

import com.example.PotteryPotSchool.dto.Solutions.*;
import com.example.PotteryPotSchool.entity.Grades.GradeEntity;
import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Solutions.SolutionOwnerType;
import com.example.PotteryPotSchool.repository.GradeRepository;
import com.example.PotteryPotSchool.repository.SolutionVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SolutionMapper {

    private final SolutionVoteRepository voteRepository;
    private final GradeRepository gradeRepository;

    public Solution toDto(SolutionEntity e) {
        return Solution.builder()
                .id(e.getId())
                .postId(e.getPost().getId())
                .ownerType(e.getOwnerType())
                .studentId(e.getStudentId())
                .studentName(e.getStudentName())
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
                .studentName(e.getStudentName())
                .teamId(e.getTeamId())
                .status(e.getStatus())
                .submittedAt(e.getSubmittedAt())
                .authorStudentId(e.getStudentId())
                .votesCount((int) voteRepository.countBySolutionId(e.getId()))
                .build();
    }

    public SolutionDetailsDto toDetailsDto(SolutionEntity e) {

        List<GradeEntity> grades = gradeRepository.findAllBySolution_Id(e.getId());

        SolutionDetailsDto dto = SolutionDetailsDto.builder()
                .id(e.getId())
                .postId(e.getPost().getId())
                .ownerType(e.getOwnerType())
                .studentId(e.getStudentId())
                .studentName(e.getStudentName())
                .teamId(e.getTeamId())
                .status(e.getStatus())
                .text(e.getText())
                .videoUrl(e.getVideoUrl())
                .attachmentUrl(e.getAttachmentUrl())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .submittedAt(e.getSubmittedAt())
                .authorStudentId(e.getStudentId())
                .teamGrade(e.getTeamGrade())
                .votesCount((int) voteRepository.countBySolutionId(e.getId()))
                .build();
        dto.setMemberGrades(
                grades.stream().map(g ->
                        MemberGradeDto.builder()
                                .solutionId(e.getId())
                                .studentId(g.getStudentId())
                                .score(g.getScore())
                                .teacherComment(g.getTeacherComment())
                                .gradedAt(g.getGradedAt())
                                .teacherId(g.getTeacherId())
                                .build()
                ).toList()
        );

        return dto;
    }
}