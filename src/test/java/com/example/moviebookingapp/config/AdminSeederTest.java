package com.example.moviebookingapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moviebookingapp.entity.User;
import com.example.moviebookingapp.enums.UserRole;
import com.example.moviebookingapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private AdminProperties adminProperties;
    private MockEnvironment environment;
    private AdminSeeder adminSeeder;

    @BeforeEach
    void setup() {
        adminProperties = new AdminProperties();
        adminProperties.setUsername("AdminUser");
        adminProperties.setEmail("ADMIN@moviebookingapp.com");
        adminProperties.setPassword("StrongAdmin1");

        environment = new MockEnvironment();

        adminSeeder = new AdminSeeder(userRepository, passwordEncoder, adminProperties, environment);
    }

    @Test
    void createsAdminWhenConfiguredAdminDoesNotExist() throws Exception {

        when(userRepository.findByUsernameOrEmail("adminuser", "admin@moviebookingapp.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongAdmin1")).thenReturn("encoded-password");

        adminSeeder.run();

        verify(userRepository).saveAndFlush(userCaptor.capture());

        User savedAdmin = userCaptor.getValue();
        assertThat(savedAdmin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(savedAdmin.getUsername()).isEqualTo("adminuser");
        assertThat(savedAdmin.getEmail()).isEqualTo("admin@moviebookingapp.com");
        assertThat(savedAdmin.getPassword()).isEqualTo("encoded-password");

        verify(passwordEncoder).encode("StrongAdmin1");
    }

    @Test
    void skipsCreationWhenConfiguredAdminUserAlreadyExists() throws Exception {

        User existingUser = new User();

        when(userRepository.findByUsernameOrEmail("adminuser", "admin@moviebookingapp.com"))
                .thenReturn(Optional.of(existingUser));

        adminSeeder.run();

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(passwordEncoder, never()).encode(any(String.class));
    }

    @Test
    void treatsConcurrentAdminCreationAsNoOp() throws Exception {

        when(userRepository.findByUsernameOrEmail("adminuser", "admin@moviebookingapp.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongAdmin1")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate admin user"));

        adminSeeder.run();

        verify(userRepository).saveAndFlush(any(User.class));
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder).encode("StrongAdmin1");
    }

    @Test
    void allowsDefaultPasswordOnlyInDevProfile() throws Exception {

        adminProperties.setPassword(null);
        environment.setActiveProfiles("dev");

        when(userRepository.findByUsernameOrEmail("adminuser", "admin@moviebookingapp.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Admin123")).thenReturn("encoded-password");

        adminSeeder.run();

        verify(userRepository).saveAndFlush(userCaptor.capture());

        User savedAdmin = userCaptor.getValue();
        assertThat(savedAdmin.getPassword()).isEqualTo("encoded-password");

        verify(passwordEncoder).encode("Admin123");
    }

    @Test
    void rejectsMissingPasswordOutsideDevProfile() {

        adminProperties.setPassword(null);

        when(userRepository.findByUsernameOrEmail("adminuser", "admin@moviebookingapp.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminSeeder.run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("ADMIN_PASSWORD must be set outside the dev profile");

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(passwordEncoder, never()).encode(any(String.class));
    }
}
