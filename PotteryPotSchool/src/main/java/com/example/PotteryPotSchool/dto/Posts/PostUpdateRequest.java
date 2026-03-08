package com.example.PotteryPotSchool.dto.Posts;

import lombok.Data;

@Data
public class PostUpdateRequest {
    private String title;
    private String description;
    private MaterialUpdateRequest material;
    private TaskUpdateRequest task;
}
