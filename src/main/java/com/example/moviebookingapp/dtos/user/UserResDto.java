package com.example.moviebookingapp.dtos.user;

import java.util.Set;

public record UserResDto(Long id, String name, String email, String phoneNumber,
    Set<String> roles) {

}
