package com.example.moviebookingapp.service;

import java.util.Locale;
import java.util.Objects;

import jakarta.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.moviebookingapp.config.JwtProperties;
import com.example.moviebookingapp.dtos.auth.ChangePasswordReqDto;
import com.example.moviebookingapp.dtos.auth.LoginReqDto;
import com.example.moviebookingapp.dtos.auth.LoginResDto;
import com.example.moviebookingapp.dtos.auth.RegisterReqDto;
import com.example.moviebookingapp.dtos.auth.RegisterResDto;
import com.example.moviebookingapp.entity.User;
import com.example.moviebookingapp.enums.UserRole;
import com.example.moviebookingapp.exception.InvalidPasswordChangeException;
import com.example.moviebookingapp.exception.UserAlreadyExistsException;
import com.example.moviebookingapp.exception.UserNotFoundException;
import com.example.moviebookingapp.mapper.UserMapper;
import com.example.moviebookingapp.repository.UserRepository;
import com.example.moviebookingapp.security.AuthenticatedUser;
import com.example.moviebookingapp.security.JwtService;
import com.example.moviebookingapp.security.TokenBlacklistService;

@Service
public class AuthService {

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            UserMapper userMapper,
            TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.userMapper = userMapper;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Transactional
    public RegisterResDto register(RegisterReqDto reqDto) {

        RegisterReqDto normalizedReqDto = normalizeRegisterRequest(reqDto);

        if (userRepository.existsByEmail(normalizedReqDto.email())) {
            throw new UserAlreadyExistsException("Email already exists: " + normalizedReqDto.email());
        }

        if (userRepository.existsByUsername(normalizedReqDto.username())) {
            throw new UserAlreadyExistsException("Username already exists: " + normalizedReqDto.username());
        }

        User user = userMapper.toEntity(normalizedReqDto);
        user.setPassword(passwordEncoder.encode(normalizedReqDto.password()));
        user.setRole(UserRole.CUSTOMER);

        User savedUser;

        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("Username or email already exists");
        }

        String token = jwtService.generateToken(AuthenticatedUser.from(savedUser));

        return new RegisterResDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                token,
                "Bearer",
                jwtProperties.getExpiresIn().toSeconds());
    }

    public LoginResDto login(LoginReqDto reqDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(reqDto.usernameOrEmail(), reqDto.password()));

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        return new LoginResDto(
                jwtService.generateToken(user),
                "Bearer",
                jwtProperties.getExpiresIn().toSeconds());
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordReqDto reqDto) {

        if (userId == null) {
            throw new InvalidPasswordChangeException("Authenticated user is required");
        }

        ChangePasswordReqDto validatedReqDto = Objects.requireNonNull(reqDto, "Password change request cannot be null");

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(validatedReqDto.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordChangeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(validatedReqDto.newPassword()));

        userRepository.save(user);
    }

    public void logout(String jti) {
        tokenBlacklistService.blacklist(jti);
    }

    private RegisterReqDto normalizeRegisterRequest(RegisterReqDto reqDto) {

        return new RegisterReqDto(
                normalizeUsername(reqDto.username()),
                normalizeEmail(reqDto.email()),
                normalizeOptionalText(reqDto.phoneNumber()),
                reqDto.password());
    }

    private String normalizeUsername(String username) {

        return normalizeRequiredText(username).toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {

        return normalizeRequiredText(email).toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(String value) {

        return value == null ? null : value.trim();
    }

    private String normalizeOptionalText(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
