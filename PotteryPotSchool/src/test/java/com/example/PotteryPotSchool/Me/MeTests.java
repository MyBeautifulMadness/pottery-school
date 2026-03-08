package com.example.PotteryPotSchool.Me;

import com.example.PotteryPotSchool.dto.Users.User;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.exception.NotFoundException;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.security.CurrentUserProvider;
import com.example.PotteryPotSchool.service.Me.Impl.MeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MeTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private MeServiceImpl meService;

    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    }

    @Test
    void getCurrentUser_returnsCurrentUser() {
        UserEntity user = UserEntity.builder()
                .id(currentUserId)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(currentUserId);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));

        User response = meService.getMe();

        assertThat(response.getId()).isEqualTo(currentUserId);
        assertThat(response.getEmail()).isEqualTo("student@test.com");
        assertThat(response.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void getCurrentUser_throws_whenUserNotFound() {

        when(currentUserProvider.getCurrentUserId()).thenReturn(currentUserId);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meService.getMe())
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getMyProfile_returnsProfile() {
        ProfileEntity profile = ProfileEntity.builder()
                .userId(currentUserId)
                .fullName("KERRIGAN")
                .about("Soldier Of Cola")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(currentUserId);
        when(profileRepository.findById(currentUserId)).thenReturn(Optional.of(profile));

        Profile response = meService.getMyProfile();

        assertThat(response.getUserId()).isEqualTo(currentUserId);
        assertThat(response.getFullName()).isEqualTo("KERRIGAN");
        assertThat(response.getAbout()).isEqualTo("Soldier of Cola");
    }

    @Test
    void getMyProfile_throws_whenProfileNotFound() {

        when(currentUserProvider.getCurrentUserId()).thenReturn(currentUserId);
        when(profileRepository.findById(currentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meService.getMyProfile())
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Profile not found");
    }
}
