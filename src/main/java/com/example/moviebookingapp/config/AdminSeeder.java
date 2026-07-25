package com.example.moviebookingapp.config;

import java.util.Locale;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.moviebookingapp.entity.User;
import com.example.moviebookingapp.enums.UserRole;
import com.example.moviebookingapp.repository.UserRepository;

@Component
@Profile("!test")
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private static final String DEV_DEFAULT_ADMIN_PASSWORD = "Admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;
    private final Environment environment;

    public AdminSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminProperties adminProperties,
            Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {

        String adminUsername = normalizeCredential(adminProperties.getUsername());
        String adminEmail = normalizeCredential(adminProperties.getEmail());

        Optional<User> existingAdmin = userRepository.findByUsernameOrEmail(adminUsername, adminEmail);

        if (existingAdmin.isPresent()) {
            log.debug("Configured admin user already exists, skipping seeding");
            return;
        }

        String adminPassword = resolveAdminPassword();

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN);

        try {
            userRepository.saveAndFlush(admin);
            log.info("Default admin user created with username: {}", admin.getUsername());
        } catch (DataIntegrityViolationException ex) {
            log.info("Configured admin user was created concurrently, skipping seeding");
            return;
        }
    }

    private String resolveAdminPassword() {

        if (StringUtils.hasText(adminProperties.getPassword())) {
            return adminProperties.getPassword();
        }

        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            log.warn("Using default admin password because ADMIN_PASSWORD is not set. Do not use this outside dev.");
            return DEV_DEFAULT_ADMIN_PASSWORD;
        }

        throw new IllegalStateException("ADMIN_PASSWORD must be set outside the dev profile");
    }

    private String normalizeCredential(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
