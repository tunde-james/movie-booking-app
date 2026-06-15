package com.example.moviebookingapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private AdminSeeder adminSeeder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void createsAdminWhenNoAdminExists() throws Exception {

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");

        adminSeeder.run();

        verify(userRepository).save(userCaptor.capture());

        User savedAdmin = userCaptor.getValue();
        assertThat(savedAdmin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(savedAdmin.getPassword()).isEqualTo("encoded-password");
        assertThat(savedAdmin.getUsername()).isNotBlank();
        assertThat(savedAdmin.getEmail()).isNotBlank();
    }

    @Test
    void skipsCreationWhenAdminAlreadyExists() throws Exception {

        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);

        adminSeeder.run();

        verify(userRepository, never()).save(any(User.class));
    }
}
