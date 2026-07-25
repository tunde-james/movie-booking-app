package com.example.moviebookingapp.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

    @NotBlank(message = "Admin username is required")
    private String username = "admin";

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email must be valid")
    private String email = "admin@moviebookingapp.com";

    private String password;
}
