package com.example.moviebookingapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moviebookingapp.config.JwtProperties;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    @Captor
    private ArgumentCaptor<JwtEncoderParameters> encoderParamsCaptor;

    @Test
    void generateTokenReturnsEncodedTokenValue() {

        AuthenticatedUser user =
                new AuthenticatedUser(1L, "johndoe", "john@example.com", "encoded-password", "CUSTOMER");

        Jwt jwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "HS256")
                .claim("sub", "johndoe")
                .build();

        when(jwtProperties.getExpiresIn()).thenReturn(Duration.ofHours(1));
        when(jwtProperties.getIssuer()).thenReturn("moviebookingapp");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = jwtService.generateToken(user);

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void generateTokenIncludesCorrectClaims() {

        AuthenticatedUser user = new AuthenticatedUser(5L, "admin", "admin@example.com", "encoded-password", "ADMIN");

        Jwt jwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "HS256")
                .claim("sub", "admin")
                .build();

        when(jwtProperties.getExpiresIn()).thenReturn(Duration.ofMinutes(30));
        when(jwtProperties.getIssuer()).thenReturn("moviebookingapp");
        when(jwtEncoder.encode(encoderParamsCaptor.capture())).thenReturn(jwt);

        jwtService.generateToken(user);

        JwtClaimsSet claims = encoderParamsCaptor.getValue().getJwsHeader() != null
                ? extractClaims(encoderParamsCaptor.getValue())
                : extractClaims(encoderParamsCaptor.getValue());

        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.<String>getClaim("iss")).isEqualTo("moviebookingapp");
        assertThat(claims.<Long>getClaim("userId")).isEqualTo(5L);
        assertThat(claims.<String>getClaim("email")).isEqualTo("admin@example.com");
        assertThat(claims.<String>getClaim("role")).isEqualTo("ADMIN");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiresAt()).isNotNull();
    }

    @Test
    void generateTokenSetsExpiryFromJwtProperties() {

        AuthenticatedUser user =
                new AuthenticatedUser(1L, "johndoe", "john@example.com", "encoded-password", "CUSTOMER");

        Jwt jwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "HS256")
                .claim("sub", "johndoe")
                .build();

        when(jwtProperties.getExpiresIn()).thenReturn(Duration.ofMinutes(45));
        when(jwtProperties.getIssuer()).thenReturn("moviebookingapp");
        when(jwtEncoder.encode(encoderParamsCaptor.capture())).thenReturn(jwt);

        Instant before = Instant.now();
        jwtService.generateToken(user);
        Instant after = Instant.now();

        JwtClaimsSet claims = extractClaims(encoderParamsCaptor.getValue());

        assertThat(claims.getIssuedAt()).isBetween(before, after);
        assertThat(claims.getExpiresAt())
                .isBetween(before.plus(Duration.ofMinutes(45)), after.plus(Duration.ofMinutes(45)));

        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    private JwtClaimsSet extractClaims(JwtEncoderParameters params) {

        try {
            var field = JwtEncoderParameters.class.getDeclaredField("claims");
            field.setAccessible(true);
            return (JwtClaimsSet) field.get(params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract claims from JwtEncoderParameters", e);
        }
    }
}
