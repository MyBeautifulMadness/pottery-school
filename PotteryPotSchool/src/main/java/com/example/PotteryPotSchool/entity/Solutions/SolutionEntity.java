package com.example.PotteryPotSchool.entity.Solutions;

import com.example.PotteryPotSchool.enums.Solutions.SolutionOwnerType;
import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import com.example.PotteryPotSchool.enums.Solutions.SolutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "solutions")
@Data
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

    @Column(nullable = false)
    private UUID studentId;

    @Column
    private String studentName;

    private UUID teamId;

    @Enumerated(EnumType.STRING)
    private SolutionOwnerType ownerType;

    @Enumerated(EnumType.STRING)
    private SolutionStatus status;

    @Column(columnDefinition = "TEXT")
    private String text;

    private String videoUrl;

    private String attachmentUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime submittedAt;

    private Integer teamGrade;
}