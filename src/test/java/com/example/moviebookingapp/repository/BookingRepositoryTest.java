package com.example.moviebookingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.moviebookingapp.config.JpaAuditingConfig;
import com.example.moviebookingapp.entity.Auditorium;
import com.example.moviebookingapp.entity.Booking;
import com.example.moviebookingapp.entity.Cinema;
import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.enums.AuditoriumType;
import com.example.moviebookingapp.enums.BookingStatus;
import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;
import com.example.moviebookingapp.enums.ShowStatus;

@SuppressWarnings("null")
@DataJpaTest
@Testcontainers
@Import(JpaAuditingConfig.class)
class BookingRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Test
    void existsByShowIdAndStatusInAndDeletedFalseReturnsTrueForPendingBooking() {

        Show show = savedShow();

        bookingRepository.saveAndFlush(booking(show, BookingStatus.PENDING, false));

        boolean exists = bookingRepository.existsByShowIdAndStatusInAndDeletedFalse(
                show.getId(), List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        assertThat(exists).isTrue();
    }

    @Test
    void existsByShowIdAndStatusInAndDeletedFalseReturnsTrueForConfirmedBooking() {

        Show show = savedShow();

        bookingRepository.saveAndFlush(booking(show, BookingStatus.CONFIRMED, false));

        boolean exists = bookingRepository.existsByShowIdAndStatusInAndDeletedFalse(
                show.getId(), List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        assertThat(exists).isTrue();
    }

    @Test
    void existsByShowIdAndStatusInAndDeletedFalseReturnsFalseForCancelledBooking() {
        Show show = savedShow();

        bookingRepository.saveAndFlush(booking(show, BookingStatus.CANCELLED, false));

        boolean exists = bookingRepository.existsByShowIdAndStatusInAndDeletedFalse(
                show.getId(), List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        assertThat(exists).isFalse();
    }

    @Test
    void existsByShowIdAndStatusInAndDeletedFalseReturnsFalseForSoftDeletedBooking() {
        Show show = savedShow();

        bookingRepository.saveAndFlush(booking(show, BookingStatus.CONFIRMED, true));

        boolean exists = bookingRepository.existsByShowIdAndStatusInAndDeletedFalse(
                show.getId(), List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        assertThat(exists).isFalse();
    }

    @Test
    void existsByShowIdAndStatusInAndDeletedFalseReturnsFalseForDifferentShow() {

        Show targetShow = savedShow("Gladiator", "Filmhouse Lekki", "Screen 1");
        Show otherShow = savedShow("Interstellar", "Genesis Maryland", "Screen 2");

        bookingRepository.saveAndFlush(booking(otherShow, BookingStatus.CONFIRMED, false));

        boolean exists = bookingRepository.existsByShowIdAndStatusInAndDeletedFalse(
                targetShow.getId(), List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        assertThat(exists).isFalse();
    }

    private Show savedShow() {

        Movie movie = movieRepository.saveAndFlush(movie("Gladiator"));
        Cinema cinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Lagos"));
        Auditorium auditorium = auditoriumRepository.saveAndFlush(auditorium(cinema, "Screen 1"));

        return showRepository.saveAndFlush(show(movie, auditorium));
    }

    private Booking booking(Show show, BookingStatus status, boolean deleted) {

        Booking booking = new Booking();
        booking.setShow(show);
        booking.setFirstName("Ada");
        booking.setLastName("Lovelace");
        booking.setEmail("ada@example.com");
        booking.setPhoneNumber(null);
        booking.setTicketQuantity(2);
        booking.setUnitPrice(new BigDecimal("3500.00"));
        booking.setTotalPrice(new BigDecimal("7000.00"));
        booking.setStatus(status);
        booking.setBookingTime(LocalDateTime.now());
        booking.setDeleted(deleted);

        return booking;
    }

    private Show show(Movie movie, Auditorium auditorium) {

        Show show = new Show();
        show.setMovie(movie);
        show.setAuditorium(auditorium);
        show.setStartTime(OffsetDateTime.parse("2026-06-01T18:30:00+01:00"));
        show.setEndTime(OffsetDateTime.parse("2026-06-01T20:45:00+01:00"));
        show.setTotalCapacity(auditorium.getCapacity());
        show.setAvailableCapacity(auditorium.getCapacity());
        show.setPricePerTicket(new BigDecimal("3500.00"));
        show.setStatus(ShowStatus.SCHEDULED);

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

    private Show savedShow(String movieTitle, String cinemaName, String auditoriumName) {
        
        Movie movie = movieRepository.saveAndFlush(movie(movieTitle));
        Cinema cinema = cinemaRepository.saveAndFlush(cinema(cinemaName, "Lagos"));
        Auditorium auditorium = auditoriumRepository.saveAndFlush(auditorium(cinema, auditoriumName));

        return showRepository.saveAndFlush(show(movie, auditorium));
    }
}
