package com.example.moviebookingapp.exception;

public class ShowNotBookableException extends RuntimeException {

    public ShowNotBookableException(String message) {
        super(message);
    }
}
