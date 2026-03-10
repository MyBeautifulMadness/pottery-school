package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Comments.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {
}
