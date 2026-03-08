package com.example.PotteryPotSchool.service.Me.Impl;

import com.example.PotteryPotSchool.dto.Profiles.Profile;
import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Profiles.ProfileEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.exception.NotFoundException;
import com.example.PotteryPotSchool.repository.ProfileRepository;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.security.CurrentUserProvider;
import com.example.PotteryPotSchool.service.Me.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeServiceImpl implements MeService {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ProfileRepository profileRepository;

    @Override
    public User getMe() {
        UUID userId = currentUserProvider.getCurrentUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        return User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }


    @Override
    public Profile getMyProfile() {
        UUID userId = currentUserProvider.getCurrentUserId();

        ProfileEntity profile = profileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found for user: " + userId));

        return new Profile(
                profile.getUserId(),
                profile.getFullName(),
                profile.getAbout()
        );
    }

}
