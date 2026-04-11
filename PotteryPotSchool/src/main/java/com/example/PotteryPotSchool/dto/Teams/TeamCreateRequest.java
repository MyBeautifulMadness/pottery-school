package com.example.PotteryPotSchool.dto.Teams;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TeamCreateRequest {
    private String name;
    private UUID captainId;
    private List<UUID> memberIds;
}