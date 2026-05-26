package com.example.moviebookingapp.exception;

public class AuditoriumAlreadyExistsException extends RuntimeException {

    public AuditoriumAlreadyExistsException(String message) {
        super(message);
    }
}
