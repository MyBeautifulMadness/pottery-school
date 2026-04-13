package com.example.PotteryPotSchool.config;

import com.example.PotteryPotSchool.entity.Profiles.ProfileEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.ProfileRepository;
import com.example.PotteryPotSchool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class StudentTestDataSeeder {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Value("${app.seed-test-students:false}")
    private boolean seedEnabled;

    @Bean
    public CommandLineRunner seedStudents() {
        return args -> {

            if (!seedEnabled) {
                return;
            }

            String markerEmail = "student01@test.local";
            if (userRepository.existsByEmail(markerEmail)) {
                return;
            }

            List<UserEntity> usersToSave = new ArrayList<>();

            for (int i = 1; i <= 20; i++) {
                String index = String.format("%02d", i);

                UserEntity user = UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("student" + index + "@test.local")
                        .password("test1234")
                        .role(Role.STUDENT)
                        .build();

                usersToSave.add(user);
            }

            List<UserEntity> savedUsers = userRepository.saveAll(usersToSave);

            List<ProfileEntity> profilesToSave = new ArrayList<>();

            for (int i = 0; i < savedUsers.size(); i++) {
                UserEntity user = savedUsers.get(i);
                String index = String.format("%02d", i + 1);

                ProfileEntity profile = ProfileEntity.builder()
                        .userId(user.getId())
                        .fullName("Student " + index)
                        .about("Тестовый студент " + index)
                        .build();

                profilesToSave.add(profile);
            }

            profileRepository.saveAll(profilesToSave);

            System.out.println("Seeded 20 test students.");
            System.out.println("Password for all students: test1234");
        };
    }
}