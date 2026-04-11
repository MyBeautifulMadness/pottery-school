package com.example.PotteryPotSchool.dto.Teams;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ManualTeamDistributionItem {
    private UUID teamId;
    private List<UUID> studentIds;
}
