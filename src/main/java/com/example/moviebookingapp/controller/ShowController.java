package com.example.moviebookingapp.controller;

import java.net.URI;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moviebookingapp.dtos.show.ShowReqDto;
import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.service.ShowService;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    @PreAuthorize("hasROle('ADMIN')")
    public ResponseEntity<ShowResDto> addShow(@Valid @RequestBody ShowReqDto reqDto) {

        ShowResDto showResDto = showService.addShow(reqDto);

        URI location = Objects.requireNonNull(
                URI.create("/api/v1/shows/" + showResDto.id()), "Created show location must not be null");

        return ResponseEntity.created(location).body(showResDto);
    }
}
