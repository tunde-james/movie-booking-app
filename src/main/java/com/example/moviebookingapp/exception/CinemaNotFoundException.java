package com.example.moviebookingapp.exception;

public class CinemaNotFoundException extends RuntimeException {

    public CinemaNotFoundException(String message) {
        super(message);
    }
}
