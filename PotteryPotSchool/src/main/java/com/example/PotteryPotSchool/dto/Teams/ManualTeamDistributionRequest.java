package com.example.PotteryPotSchool.dto.Teams;

import lombok.Data;

import java.util.List;

@Data
public class ManualTeamDistributionRequest {
    private List<ManualTeamDistributionItem> items;
}
