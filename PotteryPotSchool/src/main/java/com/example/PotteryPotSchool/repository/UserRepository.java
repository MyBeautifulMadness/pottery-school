package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRole(Role role);
    List<UserEntity> findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(Role role1, String fullName,
                                                                                          Role role2, String email);
    Optional<UserEntity>findByIdAndRole(UUID id, Role role);
}
