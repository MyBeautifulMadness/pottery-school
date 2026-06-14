package com.example.PotteryPotSchool.dto.Reviews;

import lombok.Data;

@Data
public class PeerReviewUpsertRequest {

    private Integer score;
    private String comment;
}
