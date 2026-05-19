package com.example.moviebookingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;

import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;
import com.example.moviebookingapp.repository.specification.MovieSpecifications;

@SuppressWarnings({"null"})
@DataJpaTest
@Testcontainers
class MovieRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private MovieRepository movieRepository;

    @Test
    void findAllWithPublicVisibleSpecificationReturnsOnlyComingSoonAndNowShowingMovies() {
        movieRepository.save(movie("Draft Movie", MovieStatus.DRAFT));
        Movie comingSoonMovie = movieRepository.save(movie("Coming Soon Movie", MovieStatus.COMING_SOON));
        Movie nowShowingMovie = movieRepository.save(movie("Now Showing Movie", MovieStatus.NOW_SHOWING));
        movieRepository.save(movie("Archived Movie", MovieStatus.ARCHIVED));

        List<Movie> result = movieRepository.findAll(MovieSpecifications.isPublicVisible());

        assertThat(result)
                .extracting(Movie::getTitle)
                .containsExactlyInAnyOrder(comingSoonMovie.getTitle(), nowShowingMovie.getTitle());
    }

    @Test
    void duplicateCheckUsesTitleReleaseDateAndLanguage() {
        movieRepository.save(movie("Gladiator", MovieStatus.COMING_SOON));

        boolean exists = movieRepository.existsByTitleIgnoreCaseAndReleaseDateAndLanguage(
                "gladiator", LocalDate.of(2026, 6, 1), Language.ENGLISH);

        assertThat(exists).isTrue();
    }

    private Movie movie(String title, MovieStatus status) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription("Description");
        movie.setGenre(Genre.ACTION);
        movie.setDurationInMinutes(120);
        movie.setReleaseDate(LocalDate.of(2026, 6, 1));
        movie.setLanguage(Language.ENGLISH);
        movie.setRating(MovieRating.PG_13);
        movie.setMovieStatus(status);
        movie.setPosterUrl("https://example.com/poster.jpg");
        return movie;
    }

    @Test
    void databaseRejectsDuplicateTitleReleaseDateLanguageForActiveMovies() {
        movieRepository.saveAndFlush(movie("Gladiator", MovieStatus.COMING_SOON));

        assertThatThrownBy(() -> {
                    movieRepository.saveAndFlush(movie("Gladiator", MovieStatus.NOW_SHOWING));
                })
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
