package com.example.moviebookingapp.config;

import java.time.Duration;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private static final int MIN_HMAC_SECRET_BYTES = 32;
    private static final String HMAC_SHA_256 = "HmacSHA256";

    private String secret;
    private String issuer;
    private Duration expiresIn = Duration.ofHours(1);

    @PostConstruct
    void validate() {

        Assert.hasText(secret, "app.jwt.secret must be set and cannot be blank");

        byte[] decodedSecret = decodeSecret();
        Assert.isTrue(decodedSecret.length >= MIN_HMAC_SECRET_BYTES, "app.jwt.secret must decode to at least 32 bytes");

        Assert.hasText(issuer, "app.jwt.issuer must be set and cannot be blank");
        Assert.notNull(expiresIn, "app.jwt.expires-in must be set");
    }

    public SecretKeySpec getSecretKey() {

        return new SecretKeySpec(decodeSecret(), HMAC_SHA_256);
    }

    private byte[] decodeSecret() {

        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("app.jwt.secret must be valid Base64", ex);
        }
    }
}
