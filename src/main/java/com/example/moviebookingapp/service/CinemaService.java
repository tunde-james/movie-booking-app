package com.example.moviebookingapp.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moviebookingapp.dtos.cinema.CinemaReqDto;
import com.example.moviebookingapp.dtos.cinema.CinemaResDto;
import com.example.moviebookingapp.entity.Cinema;
import com.example.moviebookingapp.mapper.CinemaMapper;
import com.example.moviebookingapp.repository.CinemaRepository;

@Service
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final CinemaMapper cinemaMapper;

    public CinemaService(CinemaRepository cinemaRepository, CinemaMapper cinemaMapper) {
        this.cinemaRepository = cinemaRepository;
        this.cinemaMapper = cinemaMapper;
    }

    @Transactional
    public CinemaResDto addCinema(CinemaReqDto reqDto) {
        CinemaReqDto normalizedReqDto = normalizeCinemaRequest(reqDto);

        Cinema cinema =
                Objects.requireNonNull(cinemaMapper.toEntity(normalizedReqDto), "Cinema mapper must not return null");

        Cinema savedCinema = cinemaRepository.save(cinema);

        return cinemaMapper.toDto(savedCinema);
    }

    private CinemaReqDto normalizeCinemaRequest(CinemaReqDto reqDto) {
        return new CinemaReqDto(
                reqDto.name().trim(), reqDto.address().trim(), reqDto.city().trim());
    }
}
