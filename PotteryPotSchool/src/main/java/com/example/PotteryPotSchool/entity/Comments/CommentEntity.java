package com.example.PotteryPotSchool.entity.Comments;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class CommentEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID postId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID authorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
