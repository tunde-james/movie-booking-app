package com.example.moviebookingapp.exception;

import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(Ordered.HIGHEST_PRECEDENCE)
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

    @ExceptionHandler(CinemaNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleCinemaNotFoundException(
            CinemaNotFoundException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ApiProblemDetails.notFound(
                request.getRequestURI(), "cinema-not-found", "Cinema not found", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.NOT_FOUND, problemDetail);
    }

    @ExceptionHandler(CinemaAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleCinemaAlreadyExistsException(
            CinemaAlreadyExistsException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ApiProblemDetails.conflict(
                request.getRequestURI(), "cinema-already-exists", "Cinema already exists", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.CONFLICT, problemDetail);
    }

    @ExceptionHandler(AuditoriumAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleAuditoriumAlreadyExistsException(
            AuditoriumAlreadyExistsException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ApiProblemDetails.conflict(
                request.getRequestURI(), "auditorium-already-exists", "Auditorium already exists", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.CONFLICT, problemDetail);
    }

    @ExceptionHandler(AuditoriumNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAuditoriumNotFoundException(
            AuditoriumNotFoundException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ApiProblemDetails.notFound(
                request.getRequestURI(), "auditorium-not-found", "Auditorium not found", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.NOT_FOUND, problemDetail);
    }

    @ExceptionHandler(InvalidShowScheduleException.class)
    public ResponseEntity<ProblemDetail> handleInvalidShowScheduleException(
            InvalidShowScheduleException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ApiProblemDetails.badRequest(
                request.getRequestURI(), "invalid-show-schedule", "Invalid show schedule", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(ShowScheduleConflictException.class)
    public ResponseEntity<ProblemDetail> handleShowScheduleConflictException(
            ShowScheduleConflictException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ApiProblemDetails.conflict(
                request.getRequestURI(), "show-schedule-conflict", "Show schedule conflict", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.CONFLICT, problemDetail);
    }

    @ExceptionHandler(ShowBookingConflictException.class)
    public ResponseEntity<ProblemDetail> handleShowBookingConflictException(
            ShowBookingConflictException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ApiProblemDetails.conflict(
                request.getRequestURI(), "show-booking-conflict", "Show booking conflict", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.CONFLICT, problemDetail);
    }

    @ExceptionHandler(ShowNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleShowNotFoundException(
            ShowNotFoundException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ApiProblemDetails.notFound(
                request.getRequestURI(), "show-not-found", "Show not found", ex.getMessage());

        return ApiProblemDetails.response(HttpStatus.NOT_FOUND, problemDetail);
    }
}
