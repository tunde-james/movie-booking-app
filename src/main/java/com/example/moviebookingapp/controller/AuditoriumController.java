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

import com.example.moviebookingapp.dtos.auditorium.AuditoriumReqDto;
import com.example.moviebookingapp.dtos.auditorium.AuditoriumResDto;
import com.example.moviebookingapp.service.AuditoriumService;

@RestController
@RequestMapping("/api/v1/cinemas/{cinemaId}/auditoriums")
public class AuditoriumController {

    private final AuditoriumService auditoriumService;

    public AuditoriumController(AuditoriumService auditoriumService) {
        this.auditoriumService = auditoriumService;
    }

    @GetMapping
    public ResponseEntity<List<AuditoriumResDto>> getAuditoriumsByCinema(@PathVariable Long cinemaId) {

        List<AuditoriumResDto> auditoriums = auditoriumService.getAuditoriumsByCinema(cinemaId);

        return ResponseEntity.ok(auditoriums);
    }

    @GetMapping("/{auditoriumId}")
    public ResponseEntity<AuditoriumResDto> getAuditoriumById(
            @PathVariable Long cinemaId, @PathVariable Long auditoriumId) {

        AuditoriumResDto auditorium = auditoriumService.getAuditoriumById(cinemaId, auditoriumId);

        return ResponseEntity.ok(auditorium);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditoriumResDto> addAuditorium(
            @PathVariable Long cinemaId, @Valid @RequestBody AuditoriumReqDto reqDto) {

        AuditoriumResDto auditoriumResDto = auditoriumService.addAuditorium(cinemaId, reqDto);

        URI location = Objects.requireNonNull(
                URI.create("/api/v1/cinemas/" + cinemaId + "/auditoriums/" + auditoriumResDto.id()),
                "Created auditorium location must not be null");

        return ResponseEntity.created(location).body(auditoriumResDto);
    }

    @PutMapping("/{auditoriumId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditoriumResDto> updateAuditorium(
            @PathVariable Long cinemaId, @PathVariable Long auditoriumId, @Valid @RequestBody AuditoriumReqDto reqDto) {

        AuditoriumResDto auditorium = auditoriumService.updateAuditorium(cinemaId, auditoriumId, reqDto);

        return ResponseEntity.ok(auditorium);
    }

    @DeleteMapping("/{auditoriumId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuditorium(@PathVariable Long cinemaId, @PathVariable Long auditoriumId) {

        auditoriumService.deleteAuditorium(cinemaId, auditoriumId);

        return ResponseEntity.noContent().build();
    }
}
