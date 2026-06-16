package com.example.moviebookingapp.controller;

import java.net.URI;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moviebookingapp.dtos.auth.LoginReqDto;
import com.example.moviebookingapp.dtos.auth.LoginResDto;
import com.example.moviebookingapp.dtos.auth.RegisterReqDto;
import com.example.moviebookingapp.dtos.auth.RegisterResDto;
import com.example.moviebookingapp.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResDto> register(@Valid @RequestBody RegisterReqDto reqDto) {

        RegisterResDto user = authService.register(reqDto);

        URI location = Objects.requireNonNull(URI.create("/api/v1/auth/" + user.id()), "");

        return ResponseEntity.created(location).body(user);
    }

    @PostMapping("/login")
    public LoginResDto login(@Valid @RequestBody LoginReqDto reqDto) {

        return authService.login(reqDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {

        authService.logout(jwt.getId());

        return ResponseEntity.noContent().build();
    }
}
