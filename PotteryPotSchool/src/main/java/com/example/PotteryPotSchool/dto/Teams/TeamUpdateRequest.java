package com.example.PotteryPotSchool.dto.Teams;

import lombok.Data;

import java.util.UUID;

@Data
public class TeamUpdateRequest {
    private String name;
    private UUID captainId;
}