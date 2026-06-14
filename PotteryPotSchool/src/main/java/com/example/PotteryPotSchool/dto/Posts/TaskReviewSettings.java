package com.example.PotteryPotSchool.dto.Posts;

import com.example.PotteryPotSchool.enums.Posts.ReviewType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskReviewSettings {

    private ReviewType reviewType;

    @Schema(nullable = true, description = "Сколько чужих работ должен проверить каждый студент (только для PEER_TO_PEER).")
    private Integer reviewsPerStudent;

    @Schema(nullable = true, description = "Дедлайн проверки работ студентами (только для PEER_TO_PEER).")
    private LocalDateTime reviewDeadline;
}
