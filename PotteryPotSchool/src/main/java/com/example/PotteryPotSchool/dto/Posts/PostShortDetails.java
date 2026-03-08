package com.example.PotteryPotSchool.dto.Posts;

import com.example.PotteryPotSchool.enums.Posts.PostType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PostShortDetails {
    private UUID id;
    private PostType type;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
