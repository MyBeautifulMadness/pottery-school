package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
