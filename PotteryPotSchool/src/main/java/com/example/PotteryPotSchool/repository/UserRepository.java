package com.example.PotteryPotSchool.repository;

import com.example.PotteryPotSchool.dto.Students.StudentSummaryDto;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRole(Role role);

    @Query("""
        SELECT new com.example.PotteryPotSchool.dto.Students.StudentSummaryDto(
            u.id,
            p.fullName
        )
        FROM UserEntity u
        JOIN ProfileEntity p ON p.userId = u.id
        WHERE u.role = 'STUDENT'
    """)
    Page<StudentSummaryDto> findStudents(Pageable pageable);

    @Query("""
    SELECT new com.example.PotteryPotSchool.dto.Students.StudentSummaryDto(
        u.id,
        p.fullName
    )
    FROM UserEntity u
    JOIN ProfileEntity p ON p.userId = u.id
    WHERE u.role = 'STUDENT'
    AND (
        LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<StudentSummaryDto> searchStudents(String query, Pageable pageable);
    Optional<UserEntity>findByIdAndRole(UUID id, Role role);
}
