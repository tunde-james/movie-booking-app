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

import com.example.moviebookingapp.dtos.cinema.CinemaReqDto;
import com.example.moviebookingapp.dtos.cinema.CinemaResDto;
import com.example.moviebookingapp.service.CinemaService;

@RestController
@RequestMapping("/api/v1/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {

        this.cinemaService = cinemaService;
    }

    @GetMapping
    public ResponseEntity<List<CinemaResDto>> getCinemas() {

        List<CinemaResDto> cinemas = cinemaService.getCinemas();

        return ResponseEntity.ok(cinemas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CinemaResDto> getCinemaById(@PathVariable Long id) {

        CinemaResDto cinema = cinemaService.getCinemaById(id);

        return ResponseEntity.ok(cinema);
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaResDto> addCinema(@Valid @RequestBody CinemaReqDto reqDto) {

        CinemaResDto cinemaResDto = cinemaService.addCinema(reqDto);

        URI location = Objects.requireNonNull(
                URI.create("/api/v1/cinemas/" + cinemaResDto.id()), "Created cinema location must not be null");

        return ResponseEntity.created(location).body(cinemaResDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaResDto> updateCinema(@PathVariable Long id, @Valid @RequestBody CinemaReqDto reqDto) {

        CinemaResDto cinemaResDto = cinemaService.updateCinema(id, reqDto);

        return ResponseEntity.ok(cinemaResDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCinema(@PathVariable Long id) {

        cinemaService.deleteCinema(id);

        return ResponseEntity.noContent().build();
    }
}
