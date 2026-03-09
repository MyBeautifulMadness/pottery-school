package com.example.PotteryPotSchool.entity.Users;

import com.example.PotteryPotSchool.enums.Users.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

}
