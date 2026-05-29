package com.example.moviebookingapp.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moviebookingapp.dtos.show.ShowReqDto;
import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.dtos.show.ShowSearchCriteria;
import com.example.moviebookingapp.entity.Auditorium;
import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.enums.ShowStatus;
import com.example.moviebookingapp.exception.AuditoriumNotFoundException;
import com.example.moviebookingapp.exception.InvalidShowScheduleException;
import com.example.moviebookingapp.exception.MovieNotFoundException;
import com.example.moviebookingapp.exception.ShowNotFoundException;
import com.example.moviebookingapp.exception.ShowScheduleConflictException;
import com.example.moviebookingapp.mapper.ShowMapper;
import com.example.moviebookingapp.repository.AuditoriumRepository;
import com.example.moviebookingapp.repository.MovieRepository;
import com.example.moviebookingapp.repository.ShowRepository;
import com.example.moviebookingapp.repository.specification.ShowSpecifications;

@Service
public class ShowService {

    private static final long CLEANUP_BUFFER_MINUTES = 15;

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final ShowMapper showMapper;

    public ShowService(
            ShowRepository showRepository,
            MovieRepository movieRepository,
            AuditoriumRepository auditoriumRepository,
            ShowMapper showMapper) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.showMapper = showMapper;
    }

    @Transactional(readOnly = true)
    public List<ShowResDto> searchShows(ShowSearchCriteria criteria) {

        Long movieId = criteria == null ? null : criteria.movieId();
        Long cinemaId = criteria == null ? null : criteria.cinemaId();
        Long auditoriumId = criteria == null ? null : criteria.auditoriumId();
        java.time.LocalDate date = criteria == null ? null : criteria.date();
        ShowStatus status = criteria == null ? null : criteria.status();
        String movieTitle = criteria == null ? null : criteria.movieTitle();
        String cinemaName = criteria == null ? null : criteria.cinemaName();
        String city = criteria == null ? null : criteria.city();

        Specification<Show> specification = ShowSpecifications.hasMovieId(movieId)
                .and(ShowSpecifications.movieTitleContains(movieTitle))
                .and(ShowSpecifications.hasCinemaId(cinemaId))
                .and(ShowSpecifications.cinemaNameContains(cinemaName))
                .and(ShowSpecifications.cinemaCityEquals(city))
                .and(ShowSpecifications.hasAuditoriumId(auditoriumId))
                .and(ShowSpecifications.startsOnDate(date))
                .and(ShowSpecifications.hasStatus(status));

        List<Show> shows = showRepository.findAll(specification);

        return showMapper.toDtoList(shows);
    }

    @Transactional(readOnly = true)
    public ShowResDto getShowById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Show ID cannot be null");
        }

        Show show = showRepository
                .findById(id)
                .orElseThrow(() -> new ShowNotFoundException("Show not found with ID: " + id));

        return showMapper.toDto(show);
    }

    @Transactional
    public ShowResDto addShow(ShowReqDto reqDto) {

        ShowReqDto validatedReqDto = Objects.requireNonNull(reqDto, "Show request cannot be null");
        Long movieId = Objects.requireNonNull(validatedReqDto.movieId(), "Movie ID cannot be null");
        Long auditoriumId = Objects.requireNonNull(validatedReqDto.auditoriumId(), "Auditorium ID cannot be null");

        validateScheduleWindow(validatedReqDto);

        Movie movie = movieRepository
                .findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));

        Auditorium auditorium = auditoriumRepository
                .findById(auditoriumId)
                .orElseThrow(() -> new AuditoriumNotFoundException("Auditorium not found with ID: " + auditoriumId));

        validateNoScheduleConflict(auditoriumId, validatedReqDto.startTime(), validatedReqDto.endTime());

        Show show = Objects.requireNonNull(
                showMapper.toEntity(validatedReqDto, movie, auditorium, ShowStatus.SCHEDULED, auditorium.getCapacity()),
                "Show mapper must not return null");

        Show savedShow = showRepository.save(show);

        return showMapper.toDto(savedShow);
    }

    private void validateScheduleWindow(ShowReqDto reqDto) {

        if (!reqDto.endTime().isAfter(reqDto.startTime())) {
            throw new InvalidShowScheduleException("Show end time must be after start time");
        }
    }

    private void validateNoScheduleConflict(Long auditoriumId, OffsetDateTime startTime, OffsetDateTime endTime) {

        OffsetDateTime bufferedStart = startTime.minusMinutes(CLEANUP_BUFFER_MINUTES);
        OffsetDateTime bufferedEnd = endTime.plusMinutes(CLEANUP_BUFFER_MINUTES);

        if (showRepository.existsOverlappingScheduledShow(
                auditoriumId, ShowStatus.SCHEDULED, bufferedStart, bufferedEnd)) {
            throw new ShowScheduleConflictException("Auditorium already has a scheduled show in this time window");
        }
    }
}
