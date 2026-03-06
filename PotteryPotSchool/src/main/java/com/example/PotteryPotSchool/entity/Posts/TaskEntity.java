package com.example.PotteryPotSchool.entity.Posts;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime deadline;

    @OneToOne
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private PostEntity post;
}
