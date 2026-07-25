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
import org.springframework.util.StringUtils;

import com.example.moviebookingapp.dtos.auth.ChangePasswordReqDto;
import com.example.moviebookingapp.dtos.auth.LoginReqDto;
import com.example.moviebookingapp.dtos.auth.LoginResDto;
import com.example.moviebookingapp.dtos.auth.RegisterReqDto;
import com.example.moviebookingapp.dtos.auth.RegisterResDto;
import com.example.moviebookingapp.service.AuthService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


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

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordReqDto reqDto, @AuthenticationPrincipal Jwt jwt) {
        
        Object userIdClaim = jwt.getClaim("userId");
        Long userId = userIdClaim instanceof Number number ? number.longValue() : null;

        authService.changePassword(userId, reqDto);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {

        String jti = jwt.getId();

        if(!StringUtils.hasText(jti)) {
            return ResponseEntity.badRequest().build();
        }

        authService.logout(jti);

        return ResponseEntity.noContent().build();
    }
}
