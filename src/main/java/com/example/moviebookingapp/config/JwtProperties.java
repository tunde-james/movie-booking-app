package com.example.moviebookingapp.config;

import java.nio.charset.StandardCharsets;
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

    private String secret;
    private String issuer;
    private Duration expiresIn = Duration.ofHours(1);

    @PostConstruct
    void validate() {

        Assert.hasText(secret, "app.jwt.secret must be set and cannot be blank");
        Assert.isTrue(secret.getBytes(StandardCharsets.UTF_8).length >= 32, "app.jwt.secret must be at least 32 bytes");
        Assert.hasText(issuer, "app.jwt.issuer must be set and cannot be blank");
        Assert.notNull(expiresIn, "app.jwt.expiration must be set");
    }

    public SecretKeySpec getSecretKey() {

        return new SecretKeySpec(getDecodedSecret(), "HmacSHA256");
    }

    private byte[] getDecodedSecret() {

        return Base64.getDecoder().decode(secret);
    }
}
