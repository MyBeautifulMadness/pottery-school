package com.example.PotteryPotSchool.service.UserActivation.impl;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.config.ForbiddenException;
import com.example.PotteryPotSchool.dto.UserActivation.UserActivationCreateRequest;
import com.example.PotteryPotSchool.dto.UserActivation.UserActivationDetails;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Profiles.ProfileEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.ProfileRepository;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.service.Me.MeService;
import com.example.PotteryPotSchool.service.UserActivation.UserActivation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActivationImpl implements UserActivation {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final MeService meService;

    @Override
    @Transactional
    public UserActivationDetails activation(UserActivationCreateRequest request) {

        validateRequest(request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Пользователь с таким email уже существует: " + request.getEmail());
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .build();

        UserEntity savedUser = userRepository.save(user);

        ProfileEntity profile = ProfileEntity.builder()
                .userId(savedUser.getId())
                .fullName(request.getFullName())
                .about(request.getAbout())
                .build();

        ProfileEntity savedProfile = profileRepository.save(profile);

        UserActivationDetails result = new UserActivationDetails();
        result.setId(savedUser.getId());
        result.setEmail(savedUser.getEmail());
        result.setRole(savedUser.getRole());
        result.setFullName(savedProfile.getFullName());
        result.setAbout(savedProfile.getAbout());

        return result;
    }

    private void validateRequest(UserActivationCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Тело запроса не должно быть пустым");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email не должен быть пустым");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Пароль не должен быть пустым");
        }
        if (request.getRole() == null) {
            throw new BadRequestException("Роль обязательна");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new BadRequestException("FullName не должен быть пустым");
        }
    }
}
