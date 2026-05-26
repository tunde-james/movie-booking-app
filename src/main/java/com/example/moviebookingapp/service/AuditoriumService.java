package com.example.moviebookingapp.service;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moviebookingapp.dtos.auditorium.AuditoriumReqDto;
import com.example.moviebookingapp.dtos.auditorium.AuditoriumResDto;
import com.example.moviebookingapp.entity.Auditorium;
import com.example.moviebookingapp.entity.Cinema;
import com.example.moviebookingapp.exception.AuditoriumAlreadyExistsException;
import com.example.moviebookingapp.exception.AuditoriumNotFoundException;
import com.example.moviebookingapp.exception.CinemaNotFoundException;
import com.example.moviebookingapp.mapper.AuditoriumMapper;
import com.example.moviebookingapp.repository.AuditoriumRepository;
import com.example.moviebookingapp.repository.CinemaRepository;

@Service
public class AuditoriumService {

    private static final String DUPLICATE_AUDITORIUM_MESSAGE =
            "An auditorium with the same name already exists in this cinema.";

    private final AuditoriumRepository auditoriumRepository;
    private final CinemaRepository cinemaRepository;
    private final AuditoriumMapper auditoriumMapper;

    public AuditoriumService(
            AuditoriumRepository auditoriumRepository,
            CinemaRepository cinemaRepository,
            AuditoriumMapper auditoriumMapper) {
        this.auditoriumRepository = auditoriumRepository;
        this.cinemaRepository = cinemaRepository;
        this.auditoriumMapper = auditoriumMapper;
    }

    @Transactional(readOnly = true)
    public List<AuditoriumResDto> getAuditoriumsByCinema(Long cinemaId) {

        if (cinemaId == null) {
            throw new IllegalArgumentException("Cinema ID cannot be null");
        }

        if (!cinemaRepository.existsById(cinemaId)) {
            throw new CinemaNotFoundException("Cinema not found with ID: " + cinemaId);
        }

        return auditoriumMapper.toDtoList(auditoriumRepository.findByCinemaId(cinemaId));
    }

    @Transactional(readOnly = true)
    public AuditoriumResDto getAuditoriumById(Long cinemaId, Long auditoriumId) {

        if (cinemaId == null) {
            throw new IllegalArgumentException("Cinema ID cannot be null");
        }

        if (auditoriumId == null) {
            throw new IllegalArgumentException("Auditorium ID cannot be null");
        }

        if (!cinemaRepository.existsById(cinemaId)) {
            throw new CinemaNotFoundException("Cinema not found with ID: " + cinemaId);
        }

        Auditorium auditorium = auditoriumRepository
                .findByCinemaIdAndId(cinemaId, auditoriumId)
                .orElseThrow(() -> new AuditoriumNotFoundException("Auditorium not found with ID: " + auditoriumId));

        return auditoriumMapper.toDto(auditorium);
    }

    @Transactional
    public AuditoriumResDto addAuditorium(Long cinemaId, AuditoriumReqDto reqDto) {

        if (cinemaId == null) {
            throw new IllegalArgumentException("Cinema ID cannot be null");
        }

        Cinema cinema = cinemaRepository
                .findById(cinemaId)
                .orElseThrow(() -> new CinemaNotFoundException("Cinema not found with ID: " + cinemaId));

        AuditoriumReqDto normalizedReqDto = normalizeAuditoriumRequest(reqDto);

        if (auditoriumRepository.existsByCinemaIdAndNameIgnoreCase(cinemaId, normalizedReqDto.name())) {
            throw new AuditoriumAlreadyExistsException(DUPLICATE_AUDITORIUM_MESSAGE);
        }

        Auditorium auditorium = Objects.requireNonNull(
                auditoriumMapper.toEntity(normalizedReqDto), "Auditorium mapper must not return null");

        auditorium.setCinema(cinema);

        Auditorium savedAuditorium = auditoriumRepository.save(auditorium);

        return auditoriumMapper.toDto(savedAuditorium);
    }

    @Transactional
    public AuditoriumResDto updateAuditorium(Long cinemaId, Long auditoriumId, AuditoriumReqDto reqDto) {

        if (cinemaId == null) {
            throw new IllegalArgumentException("Cinema ID cannot be null");
        }

        if (auditoriumId == null) {
            throw new IllegalArgumentException("Auditorium ID cannot be null");
        }

        if (!cinemaRepository.existsById(cinemaId)) {
            throw new CinemaNotFoundException("Cinema not found with ID: " + cinemaId);
        }

        Auditorium auditorium = auditoriumRepository
                .findByCinemaIdAndId(cinemaId, auditoriumId)
                .orElseThrow(() -> new AuditoriumNotFoundException("Auditorium not found with ID: " + auditoriumId));

        AuditoriumReqDto normalizedReqDto = normalizeAuditoriumRequest(reqDto);

        if (auditoriumRepository.existsByCinemaIdAndNameIgnoreCaseAndIdNot(
                cinemaId, normalizedReqDto.name(), auditoriumId)) {
            throw new AuditoriumAlreadyExistsException(DUPLICATE_AUDITORIUM_MESSAGE);
        }

        auditoriumMapper.updateEntityFromDto(normalizedReqDto, auditorium);

        try {
            Auditorium savedAuditorium = auditoriumRepository.save(auditorium);
            return auditoriumMapper.toDto(savedAuditorium);
        } catch (DataIntegrityViolationException ex) {
            throw new AuditoriumAlreadyExistsException(DUPLICATE_AUDITORIUM_MESSAGE);
        }
    }

    @Transactional
    public void deleteAuditorium(Long cinemaId, Long auditoriumId) {

        if (cinemaId == null) {
            throw new IllegalArgumentException("Cinema ID cannot be null");
        }

        if (auditoriumId == null) {
            throw new IllegalArgumentException("Auditorium ID cannot be null");
        }

        if (!cinemaRepository.existsById(cinemaId)) {
            throw new CinemaNotFoundException("Cinema not found with ID: " + cinemaId);
        }

        Auditorium auditorium = auditoriumRepository
                .findByCinemaIdAndId(cinemaId, auditoriumId)
                .orElseThrow(() -> new AuditoriumNotFoundException("Auditorium not found with ID: " + auditoriumId));

        auditorium.setDeleted(true);

        auditoriumRepository.save(auditorium);
    }

    private AuditoriumReqDto normalizeAuditoriumRequest(AuditoriumReqDto reqDto) {

        return new AuditoriumReqDto(reqDto.name().trim(), reqDto.type(), reqDto.capacity());
    }
}
