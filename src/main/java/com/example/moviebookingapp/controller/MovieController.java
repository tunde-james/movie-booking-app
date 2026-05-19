package com.example.moviebookingapp.controller;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moviebookingapp.dtos.movie.MovieReqDto;
import com.example.moviebookingapp.dtos.movie.MovieResDto;
import com.example.moviebookingapp.dtos.movie.MovieSearchCriteria;
import com.example.moviebookingapp.service.MovieService;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<MovieResDto>> getMovies(MovieSearchCriteria criteria) {

        List<MovieResDto> movies = movieService.searchMovies(criteria);

        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResDto> getMovieById(@PathVariable Long id) {

        MovieResDto movie = movieService.getMovieById(id);

        return ResponseEntity.ok(movie);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResDto> addMovie(@Valid @RequestBody MovieReqDto reqDto) {

        MovieResDto movieResDto = movieService.addMovie(reqDto);

        URI location = Objects.requireNonNull(
                URI.create("/api/v1/movies/" + movieResDto.id()), "Created movie location must not be null");

        return ResponseEntity.created(location).body(movieResDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResDto> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieReqDto reqDto) {

        MovieResDto movieResDto = movieService.updateMovie(id, reqDto);

        return ResponseEntity.ok().body(movieResDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {

        movieService.deleteMovie(id);

        return ResponseEntity.noContent().build();
    }
}
