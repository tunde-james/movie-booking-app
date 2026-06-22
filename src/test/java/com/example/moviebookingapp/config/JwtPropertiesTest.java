package com.example.moviebookingapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

public class JwtPropertiesTest {

    @Test
    void validateAcceptsBase64SecretThatDecodesToAtLeast32Bytes() {

        JwtProperties properties = jwtProperties(base64Secret(32), "moviebookingapp", Duration.ofHours(1));

        properties.validate();

        SecretKeySpec secretKey = properties.getSecretKey();

        assertThat(secretKey.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(secretKey.getEncoded()).hasSize(32);
    }

    @Test
    void validateRejectsBlankSecret() {

        JwtProperties properties = jwtProperties(" ", "moviebookingapp", Duration.ofHours(1));

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("app.jwt.secret must be set and cannot be blank");
    }

    @Test
    void validateRejectsInvalidBase64Secret() {

        JwtProperties properties = jwtProperties("not-valid-base64!", "moviebookingapp", Duration.ofHours(1));

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("app.jwt.secret must be valid Base64");
    }

    @Test
    void validateRejectsDecodedSecretShorterThan32Bytes() {

        JwtProperties properties = jwtProperties(base64Secret(31), "moviebookingapp", Duration.ofHours(1));

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("app.jwt.secret must decode to at least 32 bytes");
    }

    @Test
    void validateRejectsBlankIssuer() {

        JwtProperties properties = jwtProperties(base64Secret(32), " ", Duration.ofHours(1));

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("app.jwt.issuer must be set and cannot be blank");
    }

    @Test
    void validateRejectsMissingExpiration() {

        JwtProperties properties = jwtProperties(base64Secret(32), "moviebookingapp", null);

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("app.jwt.expires-in must be set");
    }

    private JwtProperties jwtProperties(String secret, String issuer, Duration expiresIn) {

        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setIssuer(issuer);
        properties.setExpiresIn(expiresIn);
        return properties;
    }

    private String base64Secret(int length) {

        return Base64.getEncoder().encodeToString("a".repeat(length).getBytes(StandardCharsets.UTF_8));
    }
}
