package com.example.moviebookingapp.security;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.example.moviebookingapp.config.JwtProperties;

@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklist;

    public TokenBlacklistService(JwtProperties jwtProperties) {
        this(jwtProperties.getExpiresIn());
    }

    TokenBlacklistService(Duration expiry) {
        this.blacklist = Caffeine.newBuilder()
                .expireAfterWrite(expiry)
                .maximumSize(10_000)
                .build();
    }

    public void blacklist(String jti) {
        blacklist.put(jti, Boolean.TRUE);
    }

    public boolean isBlacklisted(String jti) {
        return blacklist.getIfPresent(jti) != null;
    }
}
