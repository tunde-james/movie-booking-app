package com.example.moviebookingapp.exception;

public class AuditoriumNotFoundException extends RuntimeException {

    public AuditoriumNotFoundException(String message) {
        super(message);
    }
}
