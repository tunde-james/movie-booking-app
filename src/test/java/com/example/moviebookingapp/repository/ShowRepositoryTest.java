package com.example.moviebookingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.moviebookingapp.config.JpaAuditingConfig;
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
import com.example.moviebookingapp.repository.specification.ShowSpecifications;

@SuppressWarnings("null")
@DataJpaTest
@Testcontainers
@Import(JpaAuditingConfig.class)
class ShowRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindByIdPreservesShowOffsetDateTimes() {

        Movie movie = movieRepository.saveAndFlush(movie("Gladiator"));
        Cinema cinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Lagos"));
        Auditorium auditorium = auditoriumRepository.saveAndFlush(auditorium(cinema, "Screen 1"));

        OffsetDateTime startTime = OffsetDateTime.parse("2026-06-01T18:30:00+01:00");
        OffsetDateTime endTime = OffsetDateTime.parse("2026-06-01T20:45:00+01:00");

        Show savedShow = showRepository.saveAndFlush(show(movie, auditorium, startTime, endTime, ShowStatus.SCHEDULED));

        entityManager.clear();

        Show foundShow = showRepository.findById(savedShow.getId()).orElseThrow();

        assertThat(foundShow.getStartTime()).isEqualTo(startTime);
        assertThat(foundShow.getEndTime()).isEqualTo(endTime);
    }

    @Test
    void existsOverlappingScheduledShowReturnsTrueWhenBufferedWindowOverlapsExistingShow() {

        Movie movie = movieRepository.saveAndFlush(movie("Gladiator"));
        Cinema cinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Lagos"));
        Auditorium auditorium = auditoriumRepository.saveAndFlush(auditorium(cinema, "Screen 1"));

        showRepository.saveAndFlush(show(
                movie,
                auditorium,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                ShowStatus.SCHEDULED));

        boolean exists = showRepository.existsOverlappingScheduledShow(
                auditorium.getId(),
                ShowStatus.SCHEDULED,
                OffsetDateTime.parse("2026-06-01T20:35:00+01:00"),
                OffsetDateTime.parse("2026-06-01T22:15:00+01:00"));

        assertThat(exists).isTrue();
    }

    @Test
    void existsOverlappingScheduledShowReturnsFalseWhenCleanupBufferIsRespected() {

        Movie movie = movieRepository.saveAndFlush(movie("Gladiator"));
        Cinema cinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Lagos"));
        Auditorium auditorium = auditoriumRepository.saveAndFlush(auditorium(cinema, "Screen 1"));

        showRepository.saveAndFlush(show(
                movie,
                auditorium,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                ShowStatus.SCHEDULED));

        boolean exists = showRepository.existsOverlappingScheduledShow(
                auditorium.getId(),
                ShowStatus.SCHEDULED,
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                OffsetDateTime.parse("2026-06-01T23:15:00+01:00"));

        assertThat(exists).isFalse();
    }

    @Test
    void cancelledShowsDoNotBlockScheduling() {

        Movie movie = movieRepository.saveAndFlush(movie("Gladiator"));
        Cinema cinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Lagos"));
        Auditorium auditorium = auditoriumRepository.saveAndFlush(auditorium(cinema, "Screen 1"));

        showRepository.saveAndFlush(show(
                movie,
                auditorium,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                ShowStatus.CANCELLED));

        boolean exists = showRepository.existsOverlappingScheduledShow(
                auditorium.getId(),
                ShowStatus.SCHEDULED,
                OffsetDateTime.parse("2026-06-01T18:15:00+01:00"),
                OffsetDateTime.parse("2026-06-01T21:00:00+01:00"));

        assertThat(exists).isFalse();
    }

    @Test
    void findAllWithSearchSpecificationsReturnsMatchingShows() {

        Movie gladiator = movieRepository.saveAndFlush(movie("Gladiator"));
        Movie interstellar = movieRepository.saveAndFlush(movie("Interstellar"));

        Cinema filmhouseLekki = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Lagos"));
        Cinema genesisAbuja = cinemaRepository.saveAndFlush(cinema("Genesis Deluxe Abuja", "Abuja"));

        Auditorium screenOne = auditoriumRepository.saveAndFlush(auditorium(filmhouseLekki, "Screen 1"));
        Auditorium imaxHall = auditoriumRepository.saveAndFlush(auditorium(genesisAbuja, "IMAX Hall"));

        Show matchingShow = showRepository.saveAndFlush(show(
                gladiator,
                screenOne,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                ShowStatus.SCHEDULED));

        showRepository.saveAndFlush(show(
                interstellar,
                imaxHall,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T21:00:00+01:00"),
                ShowStatus.SCHEDULED));

        Specification<Show> specification = ShowSpecifications.movieTitleContains("glad")
                .and(ShowSpecifications.cinemaNameContains("filmhouse"))
                .and(ShowSpecifications.cinemaCityEquals("lagos"))
                .and(ShowSpecifications.startsOnDate(LocalDate.of(2026, 6, 1)))
                .and(ShowSpecifications.hasStatus(ShowStatus.SCHEDULED));

        List<Show> result = showRepository.findAll(specification);

        assertThat(result).extracting(Show::getId).containsExactly(matchingShow.getId());
    }

    @Test
    void existsOverlappingScheduledShowExcludingIdIgnoresCurrentShow() {

        Movie movie = movieRepository.saveAndFlush(movie("Gladiator"));
        Cinema cinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Lagos"));
        Auditorium auditorium = auditoriumRepository.saveAndFlush(auditorium(cinema, "Screen 1"));

        Show existingShow = showRepository.saveAndFlush(show(
                movie,
                auditorium,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                ShowStatus.SCHEDULED));

        boolean exists = showRepository.existsOverlappingScheduledShowExcludingId(
                existingShow.getId(),
                auditorium.getId(),
                ShowStatus.SCHEDULED,
                OffsetDateTime.parse("2026-06-01T18:15:00+01:00"),
                OffsetDateTime.parse("2026-06-01T21:00:00+01:00"));

        assertThat(exists).isFalse();
    }

    @Test
    void existsOverlappingScheduledShowExcludingIdReturnsTrueForAnotherOverlappingShow() {

        Movie movie = movieRepository.saveAndFlush(movie("Gladiator"));
        Cinema cinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Lagos"));
        Auditorium auditorium = auditoriumRepository.saveAndFlush(auditorium(cinema, "Screen 1"));

        Show currentShow = showRepository.saveAndFlush(show(
                movie,
                auditorium,
                OffsetDateTime.parse("2026-06-01T12:00:00+01:00"),
                OffsetDateTime.parse("2026-06-01T14:00:00+01:00"),
                ShowStatus.SCHEDULED));

        showRepository.saveAndFlush(show(
                movie,
                auditorium,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                ShowStatus.SCHEDULED));

        boolean exists = showRepository.existsOverlappingScheduledShowExcludingId(
                currentShow.getId(),
                auditorium.getId(),
                ShowStatus.SCHEDULED,
                OffsetDateTime.parse("2026-06-01T18:15:00+01:00"),
                OffsetDateTime.parse("2026-06-01T21:00:00+01:00"));

        assertThat(exists).isTrue();
    }

    @Test
    void softDeletedShowsAreExcludedFromNormalReads() {

        Movie movie = movieRepository.saveAndFlush(movie("Gladiator"));
        Cinema cinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Lagos"));
        Auditorium auditorium = auditoriumRepository.saveAndFlush(auditorium(cinema, "Screen 1"));

        Show visibleShow = showRepository.saveAndFlush(show(
                movie,
                auditorium,
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                ShowStatus.SCHEDULED));

        Show deletedShow = show(
                movie,
                auditorium,
                OffsetDateTime.parse("2026-06-01T21:15:00+01:00"),
                OffsetDateTime.parse("2026-06-01T23:00:00+01:00"),
                ShowStatus.SCHEDULED);
        deletedShow.setDeleted(true);
        showRepository.saveAndFlush(deletedShow);

        List<Show> result = showRepository.findAll();

        assertThat(result).extracting(Show::getId).containsExactly(visibleShow.getId());
    }

    private Show show(
            Movie movie, Auditorium auditorium, OffsetDateTime startTime, OffsetDateTime endTime, ShowStatus status) {

        Show show = new Show();
        show.setMovie(movie);
        show.setAuditorium(auditorium);
        show.setStartTime(startTime);
        show.setEndTime(endTime);
        show.setTotalCapacity(auditorium.getCapacity());
        show.setAvailableCapacity(auditorium.getCapacity());
        show.setPricePerTicket(new BigDecimal("3500.00"));
        show.setStatus(status);
        return show;
    }

    private Movie movie(String title) {

        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription("Description");
        movie.setGenre(Genre.ACTION);
        movie.setDurationInMinutes(155);
        movie.setReleaseDate(LocalDate.of(2026, 6, 1));
        movie.setLanguage(Language.ENGLISH);
        movie.setRating(MovieRating.PG_13);
        movie.setMovieStatus(MovieStatus.NOW_SHOWING);
        movie.setPosterUrl("https://example.com/gladiator.jpg");
        return movie;
    }

    private Cinema cinema(String name, String city) {

        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress("Admiralty Way");
        cinema.setCity(city);
        return cinema;
    }

    private Auditorium auditorium(Cinema cinema, String name) {

        Auditorium auditorium = new Auditorium();
        auditorium.setCinema(cinema);
        auditorium.setName(name);
        auditorium.setType(AuditoriumType.STANDARD);
        auditorium.setCapacity(120);
        return auditorium;
    }
}
