package com.example.moviebookingapp.service;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moviebookingapp.dtos.cinema.CinemaReqDto;
import com.example.moviebookingapp.dtos.cinema.CinemaResDto;
import com.example.moviebookingapp.entity.Cinema;
import com.example.moviebookingapp.exception.CinemaAlreadyExistsException;
import com.example.moviebookingapp.exception.CinemaNotFoundException;
import com.example.moviebookingapp.mapper.CinemaMapper;
import com.example.moviebookingapp.repository.CinemaRepository;

@Service
public class CinemaService {

    private static final String DUPLICATE_CINEMA_MESSAGE = "A cinema with the same name already exists.";

    private final CinemaRepository cinemaRepository;
    private final CinemaMapper cinemaMapper;

    public CinemaService(CinemaRepository cinemaRepository, CinemaMapper cinemaMapper) {
        this.cinemaRepository = cinemaRepository;
        this.cinemaMapper = cinemaMapper;
    }

    @Transactional(readOnly = true)
    public List<CinemaResDto> getCinemas() {

        return cinemaMapper.toDtoList(cinemaRepository.findAllByDeletedFalse());
    }

    @Transactional(readOnly = true)
    public CinemaResDto getCinemaById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Cinema ID cannot be null");
        }

        Cinema cinema = cinemaRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CinemaNotFoundException("Cinema not found with ID: " + id));

        return cinemaMapper.toDto(cinema);
    }

    @Transactional
    public CinemaResDto addCinema(CinemaReqDto reqDto) {

        CinemaReqDto normalizedReqDto = normalizeCinemaRequest(reqDto);

        if (cinemaRepository.existsByNameIgnoreCase(normalizedReqDto.name())) {
            throw new CinemaAlreadyExistsException(DUPLICATE_CINEMA_MESSAGE);
        }

        Cinema cinema =
                Objects.requireNonNull(cinemaMapper.toEntity(normalizedReqDto), "Cinema mapper must not return null");

        try {
            Cinema savedCinema = cinemaRepository.save(cinema);
            return cinemaMapper.toDto(savedCinema);
        } catch (DataIntegrityViolationException ex) {
            throw new CinemaAlreadyExistsException(DUPLICATE_CINEMA_MESSAGE);
        }
    }

    @Transactional
    public CinemaResDto updateCinema(Long id, CinemaReqDto reqDto) {

        if (id == null) {
            throw new IllegalArgumentException("Cinema ID cannot be null");
        }

        Cinema cinema = cinemaRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CinemaNotFoundException("Cinema not found with ID: " + id));

        CinemaReqDto normalizedReqDto = normalizeCinemaRequest(reqDto);

        if (cinemaRepository.existsByNameIgnoreCaseAndIdNot(normalizedReqDto.name(), id)) {
            throw new CinemaAlreadyExistsException(DUPLICATE_CINEMA_MESSAGE);
        }

        cinemaMapper.updateEntityFromDto(normalizedReqDto, cinema);

        try {
            Cinema savedCinema = cinemaRepository.save(cinema);
            return cinemaMapper.toDto(savedCinema);

        } catch (DataIntegrityViolationException ex) {
            throw new CinemaAlreadyExistsException(DUPLICATE_CINEMA_MESSAGE);
        }
    }

    @Transactional
    public void deleteCinema(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Cinema ID cannot be null");
        }

        Cinema cinema = cinemaRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CinemaNotFoundException("Cinema not found with ID: " + id));

        cinema.setDeleted(true);

        cinemaRepository.save(cinema);
    }

    private CinemaReqDto normalizeCinemaRequest(CinemaReqDto reqDto) {

        return new CinemaReqDto(
                reqDto.name().trim(), reqDto.address().trim(), reqDto.city().trim());
    }
}
