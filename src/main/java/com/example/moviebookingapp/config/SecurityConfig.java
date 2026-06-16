package com.example.moviebookingapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import com.example.moviebookingapp.security.TokenBlacklistService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies", "/api/v1/movies/*")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/shows", "/api/v1/shows/*")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/cinemas", "/api/v1/cinemas/*")
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET, "/api/v1/cinemas/*/auditoriums", "/api/v1/cinemas/*/auditoriums/*")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/*")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings/*/confirm", "/api/v1/bookings/*/cancel")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/movies", "/api/v1/cinemas", "/api/v1/shows")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/movies/*", "/api/v1/cinemas/*", "/api/v1/shows/*")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/movies/*", "/api/v1/cinemas/*", "/api/v1/shows/*")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/cinemas/*/auditoriums")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cinemas/*/auditoriums/*")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cinemas/*/auditoriums/*")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {

        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    JwtEncoder jwtEncoder(JwtProperties jwtProperties) {

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(jwtProperties.getSecretKey().getEncoded()));
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties jwtProperties, TokenBlacklistService tokenBlacklistService) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(jwtProperties.getSecretKey()).build();

        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();

        OAuth2TokenValidator<Jwt> blacklistValidator = token -> {
            if (token.getId() != null && tokenBlacklistService.isBlacklisted(token.getId())) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Token has been revoked", null));
            }
            return OAuth2TokenValidatorResult.success();
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, blacklistValidator));

        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return converter;
    }
}
