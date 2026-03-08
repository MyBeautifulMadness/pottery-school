package com.example.PotteryPotSchool.service.Me.Impl;

import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.exception.NotFoundException;
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
}
