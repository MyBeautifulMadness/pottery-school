package com.example.PotteryPotSchool.dto.Comments;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommentDetails {
    private UUID id;
    private UUID postId;
    private UUID authorId;
    private String authorName;
    private String body;
    private LocalDateTime createdAt;
}
