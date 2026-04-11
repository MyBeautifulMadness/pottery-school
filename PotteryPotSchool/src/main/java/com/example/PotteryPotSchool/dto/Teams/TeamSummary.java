package com.example.PotteryPotSchool.dto.Teams;

import lombok.Data;

import java.util.UUID;

@Data
public class TeamSummary {
    private UUID id;
    private String name;
    private Integer membersCount;
    private UUID captainId;
}
