package com.example.moviebookingapp.service;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        if (userRepository.existsByEmail(reqDto.email())) {
            throw new UserAlreadyExistsException("Email already exists: " + reqDto.email());
        }

        if (userRepository.existsByUsername(reqDto.username())) {
            throw new UserAlreadyExistsException("Username already exists: " + reqDto.username());
        }

        User user = userMapper.toEntity(reqDto);
        user.setPassword(passwordEncoder.encode(reqDto.password()));
        user.setRole(UserRole.CUSTOMER);

        User savedUser = userRepository.save(user);

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

    public void logout(String jti) {
        tokenBlacklistService.blacklist(jti);
    }
}
