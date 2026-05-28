package com.example.moviebookingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moviebookingapp.dtos.show.ShowReqDto;
import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.entity.Auditorium;
import com.example.moviebookingapp.entity.Cinema;
import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.enums.AuditoriumType;
import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;
import com.example.moviebookingapp.enums.ShowStatus;
import com.example.moviebookingapp.exception.AuditoriumNotFoundException;
import com.example.moviebookingapp.exception.InvalidShowScheduleException;
import com.example.moviebookingapp.exception.MovieNotFoundException;
import com.example.moviebookingapp.exception.ShowScheduleConflictException;
import com.example.moviebookingapp.mapper.ShowMapper;
import com.example.moviebookingapp.repository.AuditoriumRepository;
import com.example.moviebookingapp.repository.MovieRepository;
import com.example.moviebookingapp.repository.ShowRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private AuditoriumRepository auditoriumRepository;

    @Mock
    private ShowMapper showMapper;

    @InjectMocks
    private ShowService showService;

    @Test
    void addShowCreatesScheduledShowWithCapacityFromAuditoriumAndReturnsResponse() {

        ShowReqDto request = new ShowReqDto(
                100L,
                20L,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                new BigDecimal("3500.00"));

        Movie movie = movie("Gladiator");
        Auditorium auditorium = auditorium("Screen 1", 120);
        Show showToSave = new Show();
        Show savedShow = new Show();

        ShowResDto response = new ShowResDto(
                1L,
                100L,
                "Gladiator",
                10L,
                "Filmhouse Lekki",
                20L,
                "Screen 1",
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                120,
                120,
                new BigDecimal("3500.00"),
                ShowStatus.SCHEDULED);

        when(movieRepository.findById(100L)).thenReturn(Optional.of(movie));
        when(auditoriumRepository.findById(20L)).thenReturn(Optional.of(auditorium));
        when(showRepository.existsOverlappingScheduledShow(
                        20L,
                        ShowStatus.SCHEDULED,
                        OffsetDateTime.parse("2026-06-01T18:15:00+01:00"),
                        OffsetDateTime.parse("2026-06-01T21:00:00+01:00")))
                .thenReturn(false);
        when(showMapper.toEntity(request, movie, auditorium, ShowStatus.SCHEDULED, 120))
                .thenReturn(showToSave);
        when(showRepository.save(showToSave)).thenReturn(savedShow);
        when(showMapper.toDto(savedShow)).thenReturn(response);

        ShowResDto result = showService.addShow(request);

        assertThat(result).isEqualTo(response);

        verify(showMapper).toEntity(request, movie, auditorium, ShowStatus.SCHEDULED, 120);
        verify(showRepository).save(showToSave);
    }

    @Test
    void addShowReturnsNotFoundWhenMovieDoesNotExist() {

        ShowReqDto request = new ShowReqDto(
                99L,
                20L,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                new BigDecimal("3500.00"));

        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> showService.addShow(request))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie not found with ID: 99");

        verify(auditoriumRepository, never()).findById(any(Long.class));
        verify(showMapper, never())
                .toEntity(
                        any(ShowReqDto.class),
                        any(Movie.class),
                        any(Auditorium.class),
                        any(ShowStatus.class),
                        any(Integer.class));
        verify(showRepository, never()).save(any(Show.class));
    }

    @Test
    void addShowReturnsNotFoundWhenAuditoriumDoesNotExist() {

        ShowReqDto request = new ShowReqDto(
                100L,
                99L,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                new BigDecimal("3500.00"));

        Movie movie = movie("Gladiator");

        when(movieRepository.findById(100L)).thenReturn(Optional.of(movie));
        when(auditoriumRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> showService.addShow(request))
                .isInstanceOf(AuditoriumNotFoundException.class)
                .hasMessage("Auditorium not found with ID: 99");

        verify(showMapper, never())
                .toEntity(
                        any(ShowReqDto.class),
                        any(Movie.class),
                        any(Auditorium.class),
                        any(ShowStatus.class),
                        any(Integer.class));
        verify(showRepository, never()).save(any(Show.class));
    }

    @Test
    void addShowRejectsEndTimeBeforeStartTime() {

        ShowReqDto request = new ShowReqDto(
                100L,
                20L,
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                new BigDecimal("3500.00"));

        assertThatThrownBy(() -> showService.addShow(request))
                .isInstanceOf(InvalidShowScheduleException.class)
                .hasMessage("Show end time must be after start time");

        verify(movieRepository, never()).findById(any(Long.class));
        verify(auditoriumRepository, never()).findById(any(Long.class));
        verify(showRepository, never()).save(any(Show.class));
    }

    @Test
    void addShowRejectsEndTimeEqualToStartTime() {

        ShowReqDto request = new ShowReqDto(
                100L,
                20L,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                new BigDecimal("3500.00"));

        assertThatThrownBy(() -> showService.addShow(request))
                .isInstanceOf(InvalidShowScheduleException.class)
                .hasMessage("Show end time must be after start time");

        verify(movieRepository, never()).findById(any(Long.class));
        verify(auditoriumRepository, never()).findById(any(Long.class));
        verify(showRepository, never()).save(any(Show.class));
    }

    @Test
    void addShowRejectsOverlappingScheduledShowWithCleanupBuffer() {

        ShowReqDto request = new ShowReqDto(
                100L,
                20L,
                OffsetDateTime.parse("2026-06-01T20:10:00+01:00"),
                OffsetDateTime.parse("2026-06-01T22:00:00+01:00"),
                new BigDecimal("3500.00"));

        Movie movie = movie("Gladiator");
        Auditorium auditorium = auditorium("Screen 1", 120);

        when(movieRepository.findById(100L)).thenReturn(Optional.of(movie));
        when(auditoriumRepository.findById(20L)).thenReturn(Optional.of(auditorium));
        when(showRepository.existsOverlappingScheduledShow(
                        20L,
                        ShowStatus.SCHEDULED,
                        OffsetDateTime.parse("2026-06-01T19:55:00+01:00"),
                        OffsetDateTime.parse("2026-06-01T22:15:00+01:00")))
                .thenReturn(true);

        assertThatThrownBy(() -> showService.addShow(request))
                .isInstanceOf(ShowScheduleConflictException.class)
                .hasMessage("Auditorium already has a scheduled show in this time window");

        verify(showMapper, never())
                .toEntity(
                        any(ShowReqDto.class),
                        any(Movie.class),
                        any(Auditorium.class),
                        any(ShowStatus.class),
                        any(Integer.class));
        verify(showRepository, never()).save(any(Show.class));
    }

    private Movie movie(String title) {

        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription("Description");
        movie.setGenre(Genre.ACTION);
        movie.setDurationInMinutes(155);
        movie.setReleaseDate(java.time.LocalDate.of(2026, 6, 1));
        movie.setLanguage(Language.ENGLISH);
        movie.setRating(MovieRating.PG_13);
        movie.setMovieStatus(MovieStatus.NOW_SHOWING);
        movie.setPosterUrl("https://example.com/gladiator.jpg");
        return movie;
    }

    private Auditorium auditorium(String name, Integer capacity) {

        Cinema cinema = new Cinema();
        cinema.setName("Filmhouse Lekki");
        cinema.setAddress("Admiralty Way");
        cinema.setCity("Lagos");

        Auditorium auditorium = new Auditorium();
        auditorium.setCinema(cinema);
        auditorium.setName(name);
        auditorium.setType(AuditoriumType.STANDARD);
        auditorium.setCapacity(capacity);
        return auditorium;
    }
}
