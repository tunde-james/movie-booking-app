package com.example.moviebookingapp.security;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.example.moviebookingapp.config.JwtProperties;

@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklistedTokens;

    public TokenBlacklistService(JwtProperties jwtProperties) {
        this.blacklistedTokens = Caffeine.newBuilder()
                .expireAfterWrite(jwtProperties.getExpiresIn())
                .maximumSize(10_000)
                .build();
    }

    public void blacklist(String jti) {
        blacklistedTokens.put(jti, Boolean.TRUE);
    }

    public boolean isBlacklisted(String jti) {
        return blacklistedTokens.getIfPresent(jti) != null;
    }
}
