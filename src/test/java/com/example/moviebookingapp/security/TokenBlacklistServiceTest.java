package com.example.moviebookingapp.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenBlacklistServiceTest {

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(Duration.ofHours(1));
    }

    @Test
    void blacklistedTokenIsDetected() {

        tokenBlacklistService.blacklist("token-id-1");

        assertThat(tokenBlacklistService.isBlacklisted("token-id-1")).isTrue();
    }

    @Test
    void unknownTokenIsNotBlacklisted() {

        assertThat(tokenBlacklistService.isBlacklisted("unknown-token-id")).isFalse();
    }

    @Test
    void multipleTokensCanBeBlacklisted() {

        tokenBlacklistService.blacklist("token-1");
        tokenBlacklistService.blacklist("token-2");

        assertThat(tokenBlacklistService.isBlacklisted("token-1")).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted("token-2")).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted("token-3")).isFalse();
    }
}
