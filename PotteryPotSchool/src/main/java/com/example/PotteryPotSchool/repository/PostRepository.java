package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Posts.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<PostEntity, UUID> {
}
