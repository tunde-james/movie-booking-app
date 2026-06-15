package com.example.moviebookingapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moviebookingapp.entity.User;
import com.example.moviebookingapp.enums.UserRole;
import com.example.moviebookingapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameReturnsAuthenticatedUserWhenFoundByUsername() {

        User user = user(1L, "johndoe", "john@example.com", "encoded-password", UserRole.CUSTOMER);

        when(userRepository.findByUsernameOrEmail("johndoe", "johndoe")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertThat(result).isInstanceOf(AuthenticatedUser.class);

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) result;
        assertThat(authenticatedUser.id()).isEqualTo(1L);
        assertThat(authenticatedUser.getUsername()).isEqualTo("johndoe");
        assertThat(authenticatedUser.email()).isEqualTo("john@example.com");
        assertThat(authenticatedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(authenticatedUser.role()).isEqualTo("CUSTOMER");
        assertThat(authenticatedUser.getAuthorities()).extracting("authority").containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void loadUserByUsernameReturnsAuthenticatedUserWhenFoundByEmail() {

        User user = user(2L, "admin", "admin@example.com", "encoded-password", UserRole.ADMIN);

        when(userRepository.findByUsernameOrEmail("admin@example.com", "admin@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin@example.com");

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) result;
        assertThat(authenticatedUser.id()).isEqualTo(2L);
        assertThat(authenticatedUser.getUsername()).isEqualTo("admin");
        assertThat(authenticatedUser.role()).isEqualTo("ADMIN");
        assertThat(authenticatedUser.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsernameThrowsWhenUserNotFound() {

        when(userRepository.findByUsernameOrEmail("unknown", "unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid username/email or password");
    }

    private User user(Long id, String username, String email, String password, UserRole role) {

        User user = new User();
        try {
            var idField = com.example.moviebookingapp.entity.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}
