package com.example.moviebookingapp.exception;

public class CinemaAlreadyExistsException extends RuntimeException {

    public CinemaAlreadyExistsException(String message) {
        super(message);
    }
}
