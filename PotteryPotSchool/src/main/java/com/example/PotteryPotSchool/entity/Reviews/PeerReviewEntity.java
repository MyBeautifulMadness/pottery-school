package com.example.PotteryPotSchool.entity.Reviews;

import com.example.PotteryPotSchool.entity.Solutions.SolutionEntity;
import com.example.PotteryPotSchool.enums.Solutions.PeerReviewStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "peer_reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"solution_id", "reviewer_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeerReviewEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_id", nullable = false)
    private SolutionEntity solution;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    private String reviewerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeerReviewStatus status;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
