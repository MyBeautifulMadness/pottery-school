package com.example.PotteryPotSchool.UserActivation;

import com.example.PotteryPotSchool.config.BadRequestException;
import com.example.PotteryPotSchool.dto.UserActivation.UserActivationCreateRequest;
import com.example.PotteryPotSchool.dto.UserActivation.UserActivationDetails;
import com.example.PotteryPotSchool.entity.Profiles.ProfileEntity;
import com.example.PotteryPotSchool.entity.Users.UserEntity;
import com.example.PotteryPotSchool.enums.Users.Role;
import com.example.PotteryPotSchool.repository.ProfileRepository;
import com.example.PotteryPotSchool.repository.UserRepository;
import com.example.PotteryPotSchool.service.UserActivation.UserActivation;
import com.example.PotteryPotSchool.service.UserActivation.impl.UserActivationImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserActivationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private UserActivationImpl userManagementService;

    @Test
    void shouldCreateFirstUserInUsersAndProfiles() {
        UserActivationCreateRequest request = new UserActivationCreateRequest();
        request.setEmail("teacher1@example.com");
        request.setPassword("password1");
        request.setRole(Role.TEACHER);
        request.setFullName("Первый преподаватель");
        request.setAbout("Преподаватель по гончарному мастерству");

        when(userRepository.findByEmail("teacher1@example.com")).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserActivationDetails result = userManagementService.activation(request);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("teacher1@example.com", result.getEmail());
        assertEquals(Role.TEACHER, result.getRole());
        assertEquals("Первый преподаватель", result.getFullName());
        assertEquals("Преподаватель по гончарному мастерству", result.getAbout());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertEquals("teacher1@example.com", savedUser.getEmail());
        assertEquals("password1", savedUser.getPassword());
        assertEquals(Role.TEACHER, savedUser.getRole());

        ArgumentCaptor<ProfileEntity> profileCaptor = ArgumentCaptor.forClass(ProfileEntity.class);
        verify(profileRepository).save(profileCaptor.capture());

        ProfileEntity savedProfile = profileCaptor.getValue();
        assertEquals(userId, savedProfile.getUserId());
        assertEquals("Первый преподаватель", savedProfile.getFullName());
        assertEquals("Преподаватель по гончарному мастерству", savedProfile.getAbout());
    }

    @Test
    void shouldCreateSecondUserInUsersAndProfiles() {
        UserActivationCreateRequest request = new UserActivationCreateRequest();
        request.setEmail("student1@example.com");
        request.setPassword("password2");
        request.setRole(Role.STUDENT);
        request.setFullName("Первый студент");
        request.setAbout("Новичок");

        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserActivationDetails result = userManagementService.activation(request);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("student1@example.com", result.getEmail());
        assertEquals(Role.STUDENT, result.getRole());
        assertEquals("Первый студент", result.getFullName());
        assertEquals("Новичок", result.getAbout());
    }

    @Test
    void shouldCreateThirdUserInUsersAndProfiles() {
        UserActivationCreateRequest request = new UserActivationCreateRequest();
        request.setEmail("teacher2@example.com");
        request.setPassword("password3");
        request.setRole(Role.TEACHER);
        request.setFullName("Второй учитель");
        request.setAbout("Ведет практику");

        when(userRepository.findByEmail("teacher2@example.com")).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserActivationDetails result = userManagementService.activation(request);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("teacher2@example.com", result.getEmail());
        assertEquals(Role.TEACHER, result.getRole());
        assertEquals("Второй учитель", result.getFullName());
        assertEquals("Ведет практику", result.getAbout());
    }

    @Test
    void shouldCreateFourthUserInUsersAndProfiles() {
        UserActivationCreateRequest request = new UserActivationCreateRequest();
        request.setEmail("student2@example.com");
        request.setPassword("password4");
        request.setRole(Role.STUDENT);
        request.setFullName("Второй студент");
        request.setAbout("Любит лепку");

        when(userRepository.findByEmail("student2@example.com")).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserActivationDetails result = userManagementService.activation(request);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("student2@example.com", result.getEmail());
        assertEquals(Role.STUDENT, result.getRole());
        assertEquals("Второй студент", result.getFullName());
        assertEquals("Любит лепку", result.getAbout());
    }

    @Test
    void shouldCreateFifthUserInUsersAndProfiles() {
        UserActivationCreateRequest request = new UserActivationCreateRequest();
        request.setEmail("teacher3@example.com");
        request.setPassword("password5");
        request.setRole(Role.TEACHER);
        request.setFullName("Третий учитель");
        request.setAbout("Куратор группы");

        when(userRepository.findByEmail("teacher3@example.com")).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserActivationDetails result = userManagementService.activation(request);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("teacher3@example.com", result.getEmail());
        assertEquals(Role.TEACHER, result.getRole());
        assertEquals("Третий учитель", result.getFullName());
        assertEquals("Куратор группы", result.getAbout());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenEmailAlreadyExists() {
        UserActivationCreateRequest request = new UserActivationCreateRequest();
        request.setEmail("teacher1@example.com");
        request.setPassword("password1");
        request.setRole(Role.TEACHER);
        request.setFullName("Первый учитель");
        request.setAbout("Описание");

        when(userRepository.findByEmail("teacher1@example.com"))
                .thenReturn(Optional.of(UserEntity.builder()
                        .id(UUID.randomUUID())
                        .email("teacher1@example.com")
                        .password("old-password")
                        .role(Role.TEACHER)
                        .build()));

        assertThrows(BadRequestException.class, () -> userManagementService.activation(request));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(profileRepository, never()).save(any(ProfileEntity.class));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenEmailIsBlank() {
        UserActivationCreateRequest request = new UserActivationCreateRequest();
        request.setEmail("   ");
        request.setPassword("password");
        request.setRole(Role.STUDENT);
        request.setFullName("Студент");
        request.setAbout("Описание");

        assertThrows(BadRequestException.class, () -> userManagementService.activation(request));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(profileRepository, never()).save(any(ProfileEntity.class));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenPasswordIsBlank() {
        UserActivationCreateRequest request = new UserActivationCreateRequest();
        request.setEmail("student@example.com");
        request.setPassword("   ");
        request.setRole(Role.STUDENT);
        request.setFullName("Студент");
        request.setAbout("Описание");

        assertThrows(BadRequestException.class, () -> userManagementService.activation(request));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(profileRepository, never()).save(any(ProfileEntity.class));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenFullNameIsBlank() {
        UserActivationCreateRequest request = new UserActivationCreateRequest();
        request.setEmail("student@example.com");
        request.setPassword("password");
        request.setRole(Role.STUDENT);
        request.setFullName("   ");
        request.setAbout("Описание");

        assertThrows(BadRequestException.class, () -> userManagementService.activation(request));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(profileRepository, never()).save(any(ProfileEntity.class));
    }
}
