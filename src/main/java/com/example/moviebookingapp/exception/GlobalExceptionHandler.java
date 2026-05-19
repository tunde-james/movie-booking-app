package com.example.moviebookingapp.exception;

import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(), Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value")))
                .toList();

        ProblemDetail problemDetail = ApiProblemDetails.validationError(request.getRequestURI(), errors);

        return ApiProblemDetails.response(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(MovieAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleMovieAlreadyExistsException(
            MovieAlreadyExistsException ex, HttpServletRequest request) {

        log.warn("Movie already exists: {}", ex.getMessage());

        ProblemDetail problemDetail = ApiProblemDetails.conflict(
                request.getRequestURI(), "movie-already-exists", "Movie already exists", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.CONFLICT, problemDetail);
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleMovieNotFoundException(
            MovieNotFoundException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ApiProblemDetails.notFound(
                request.getRequestURI(), "movie-not-found", "Movie not found", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.NOT_FOUND, problemDetail);
    }
}
