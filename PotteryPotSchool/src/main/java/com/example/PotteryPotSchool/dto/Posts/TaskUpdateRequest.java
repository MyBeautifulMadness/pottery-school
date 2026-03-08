package com.example.PotteryPotSchool.dto.Posts;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskUpdateRequest {
    private String description;
    private LocalDateTime deadline;
}
