package com.example.PotteryPotSchool.dto.Teams;

import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class Team {
    private UUID id;
    private UUID postId;
    private String name;
    private UUID captainId;
    private List<StudentSummaryDto> members;
    private LocalDateTime createdAt;
}