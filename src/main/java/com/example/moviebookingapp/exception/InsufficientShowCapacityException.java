package com.example.moviebookingapp.exception;

public class InsufficientShowCapacityException extends RuntimeException {

    public InsufficientShowCapacityException(String message) {
        super(message);
    }
}