package com.example.moviebookingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moviebookingapp.config.JwtProperties;
import com.example.moviebookingapp.dtos.auth.LoginReqDto;
import com.example.moviebookingapp.dtos.auth.LoginResDto;
import com.example.moviebookingapp.dtos.auth.RegisterReqDto;
import com.example.moviebookingapp.dtos.auth.RegisterResDto;
import com.example.moviebookingapp.entity.User;
import com.example.moviebookingapp.enums.UserRole;
import com.example.moviebookingapp.exception.UserAlreadyExistsException;
import com.example.moviebookingapp.mapper.UserMapper;
import com.example.moviebookingapp.repository.UserRepository;
import com.example.moviebookingapp.security.AuthenticatedUser;
import com.example.moviebookingapp.security.JwtService;
import com.example.moviebookingapp.security.TokenBlacklistService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesCustomerWithEncodedPasswordAndReturnsToken() {

        RegisterReqDto request = new RegisterReqDto("johndoe", "john@example.com", "+2348012345678", "Password1");

        User mappedUser = user("johndoe", "john@example.com", "+2348012345678");
        User savedUser = savedUser(1L, "johndoe", "john@example.com", "+2348012345678");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(jwtService.generateToken(any(AuthenticatedUser.class))).thenReturn("jwt-token");
        when(jwtProperties.getExpiresIn()).thenReturn(Duration.ofHours(1));

        RegisterResDto result = authService.register(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("johndoe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.role()).isEqualTo("CUSTOMER");
        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresInSeconds()).isEqualTo(3600L);

        assertThat(mappedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(mappedUser.getRole()).isEqualTo(UserRole.CUSTOMER);

        verify(userMapper).toEntity(request);
        verify(passwordEncoder).encode("Password1");
        verify(userRepository).save(mappedUser);
        verify(jwtService).generateToken(any(AuthenticatedUser.class));
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {

        RegisterReqDto request = new RegisterReqDto("johndoe", "john@example.com", null, "Password1");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already exists: john@example.com");

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {

        RegisterReqDto request = new RegisterReqDto("johndoe", "john@example.com", null, "Password1");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username already exists: johndoe");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerChecksEmailBeforeUsername() {

        RegisterReqDto request = new RegisterReqDto("johndoe", "john@example.com", null, "Password1");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {

        LoginReqDto request = new LoginReqDto("johndoe", "Password1");

        AuthenticatedUser authenticatedUser =
                new AuthenticatedUser(1L, "johndoe", "john@example.com", "encoded-password", "CUSTOMER");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(authenticatedUser)).thenReturn("jwt-token");
        when(jwtProperties.getExpiresIn()).thenReturn(Duration.ofHours(1));

        LoginResDto result = authService.login(request);

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresInSeconds()).isEqualTo(3600L);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(authenticatedUser);
    }

    @Test
    void loginPropagatesBadCredentialsException() {

        LoginReqDto request = new LoginReqDto("johndoe", "WrongPassword1");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username/email or password"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username/email or password");

        verify(jwtService, never()).generateToken(any(AuthenticatedUser.class));
    }

    @Test
    void logoutBlacklistsTokenId() {

        authService.logout("jwt-id-123");
    }

    private User user(String username, String email, String phoneNumber) {

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        return user;
    }

    private User savedUser(Long id, String username, String email, String phoneNumber) {

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
        user.setPhoneNumber(phoneNumber);
        user.setRole(UserRole.CUSTOMER);
        user.setPassword("encoded-password");
        return user;
    }
}
