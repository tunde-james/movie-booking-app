package com.example.moviebookingapp.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.moviebookingapp.entity.User;

public record AuthenticatedUser(Long id, String username, String email, String password, String role)
        implements UserDetails {

    public static AuthenticatedUser from(User user) {

        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {

        return password;
    }

    @Override
    public String getUsername() {

        return username;
    }
}
