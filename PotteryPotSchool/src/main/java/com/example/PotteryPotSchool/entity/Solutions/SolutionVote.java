package com.example.PotteryPotSchool.entity.Solutions;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "solution_votes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolutionVote {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID solutionId;

    private UUID studentId;

    private UUID postId;
}
