package com.example.PotteryPotSchool.entity.Solutions;

import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "solutions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolutionEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @JoinColumn(name = "student_id", nullable = false)
    private UUID student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SolutionStatus status;

    @Column(columnDefinition = "TEXT")
    private String text;

    private String videoUrl;

    private String attachmentUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime submittedAt;
}
