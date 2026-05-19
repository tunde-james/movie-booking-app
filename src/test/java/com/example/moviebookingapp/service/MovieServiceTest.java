package com.example.moviebookingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moviebookingapp.dtos.movie.MovieReqDto;
import com.example.moviebookingapp.dtos.movie.MovieResDto;
import com.example.moviebookingapp.dtos.movie.MovieSearchCriteria;
import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;
import com.example.moviebookingapp.exception.MovieAlreadyExistsException;
import com.example.moviebookingapp.exception.MovieNotFoundException;
import com.example.moviebookingapp.mapper.MovieMapper;
import com.example.moviebookingapp.repository.MovieRepository;

@SuppressWarnings({"unchecked", "null"})
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    @Test
    void getPublicMoviesReturnsComingSoonAndNowShowingMovies() {
        Movie comingSoonMovie = movieWithStatus("Interstellar", MovieStatus.COMING_SOON);
        Movie nowShowingMovie = movieWithStatus("Inception", MovieStatus.NOW_SHOWING);

        MovieResDto comingSoonDto = movieDto(1L, "Interstellar", MovieStatus.COMING_SOON);
        MovieResDto nowShowingDto = movieDto(2L, "Inception", MovieStatus.NOW_SHOWING);

        when(movieRepository.findAll(any(Specification.class))).thenReturn(List.of(comingSoonMovie, nowShowingMovie));

        when(movieMapper.toDtoList(List.of(comingSoonMovie, nowShowingMovie)))
                .thenReturn(List.of(comingSoonDto, nowShowingDto));

        List<MovieResDto> result = movieService.getPublicMovies();

        assertThat(result)
                .extracting(MovieResDto::movieStatus)
                .containsExactlyInAnyOrder(MovieStatus.COMING_SOON, MovieStatus.NOW_SHOWING);

        verify(movieRepository).findAll(any(Specification.class));
    }

    private Movie movieWithStatus(String title, MovieStatus status) {

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

    private MovieResDto movieDto(Long id, String title, MovieStatus status) {

        return new MovieResDto(
                id,
                title,
                "Description",
                Genre.ACTION,
                120,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                status,
                "https://example.com/poster.jpg");
    }

    @Test
    void searchMoviesUsesSpecificationsAndMapsResults() {
        MovieSearchCriteria criteria =
                new MovieSearchCriteria("Inter", Genre.SCI_FI, Language.ENGLISH, MovieStatus.COMING_SOON);

        Movie movie = movieWithStatus("Interstellar", MovieStatus.COMING_SOON);
        MovieResDto dto = movieDto(1L, "Interstellar", MovieStatus.COMING_SOON);

        when(movieRepository.findAll(any(Specification.class))).thenReturn(List.of(movie));
        when(movieMapper.toDtoList(List.of(movie))).thenReturn(List.of(dto));

        List<MovieResDto> result = movieService.searchMovies(criteria);

        assertThat(result).hasSize(1).first().extracting(MovieResDto::title).isEqualTo("Interstellar");

        verify(movieRepository).findAll(any(Specification.class));
    }

    @Test
    void addMovieRejectsExactDuplicateTitleReleaseDateAndLanguage() {
        MovieReqDto request = new MovieReqDto(
                "Gladiator",
                "A historical action drama.",
                Genre.ACTION,
                155,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/gladiator.jpg");

        when(movieRepository.existsByTitleIgnoreCaseAndReleaseDateAndLanguage(
                        "Gladiator", LocalDate.of(2026, 6, 1), Language.ENGLISH))
                .thenReturn(true);

        assertThatThrownBy(() -> movieService.addMovie(request))
                .isInstanceOf(MovieAlreadyExistsException.class)
                .hasMessage("A movie with the same title, release date, and language already exists.");

        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void addMovieSavesMovieAndReturnsResponse() {
        MovieReqDto request = new MovieReqDto(
                "Gladiator",
                "A historical action drama.",
                Genre.ACTION,
                155,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/gladiator.jpg");

        Movie movieToSave = movieWithStatus("Gladiator", MovieStatus.COMING_SOON);
        Movie savedMovie = movieWithStatus("Gladiator", MovieStatus.COMING_SOON);
        MovieResDto response = movieDto(1L, "Gladiator", MovieStatus.COMING_SOON);

        when(movieRepository.existsByTitleIgnoreCaseAndReleaseDateAndLanguage(
                        "Gladiator", LocalDate.of(2026, 6, 1), Language.ENGLISH))
                .thenReturn(false);
        when(movieMapper.toEntity(request)).thenReturn(movieToSave);
        when(movieRepository.save(movieToSave)).thenReturn(savedMovie);
        when(movieMapper.toDto(savedMovie)).thenReturn(response);

        MovieResDto result = movieService.addMovie(request);

        assertThat(result).isEqualTo(response);

        verify(movieRepository).save(movieToSave);
    }

    @Test
    void addMovieTranslatesDatabaseDuplicateViolationToMovieAlreadyExistsException() {
        MovieReqDto request = new MovieReqDto(
                "Gladiator",
                "A historical action drama.",
                Genre.ACTION,
                155,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/gladiator.jpg");

        Movie movieToSave = movieWithStatus("Gladiator", MovieStatus.COMING_SOON);

        when(movieRepository.existsByTitleIgnoreCaseAndReleaseDateAndLanguage(
                        "Gladiator", LocalDate.of(2026, 6, 1), Language.ENGLISH))
                .thenReturn(false);
        when(movieMapper.toEntity(request)).thenReturn(movieToSave);
        when(movieRepository.save(movieToSave)).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThatThrownBy(() -> movieService.addMovie(request))
                .isInstanceOf(MovieAlreadyExistsException.class)
                .hasMessage("A movie with the same title, release date, and language already exists.");
    }

    @Test
    void getMovieByIdThrowsMovieNotFoundExceptionWhenMovieDoesNotExist() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieById(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie not found with ID: 99");
    }

    @Test
    void getMovieByIdReturnsMovieWhenItExists() {
        Movie movie = movieWithStatus("Interstellar", MovieStatus.COMING_SOON);
        MovieResDto response = movieDto(1L, "Interstellar", MovieStatus.COMING_SOON);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieMapper.toDto(movie)).thenReturn(response);

        MovieResDto result = movieService.getMovieById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void updateMovieUpdatesExistingMovieAndReturnsResponse() {
        MovieReqDto request = new MovieReqDto(
                "Gladiator Updated",
                "Updated description.",
                Genre.ACTION,
                160,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/gladiator-updated.jpg");

        Movie existingMovie = movieWithStatus("Gladiator", MovieStatus.COMING_SOON);
        Movie savedMovie = movieWithStatus("Gladiator Updated", MovieStatus.COMING_SOON);
        MovieResDto response = movieDto(1L, "Gladiator Updated", MovieStatus.COMING_SOON);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(existingMovie)).thenReturn(savedMovie);
        when(movieMapper.toDto(savedMovie)).thenReturn(response);

        MovieResDto result = movieService.updateMovie(1L, request);

        assertThat(result).isEqualTo(response);

        verify(movieMapper).updateEntityFromDto(request, existingMovie);
        verify(movieRepository).save(existingMovie);
    }

    @Test
    void updateMovieThrowsMovieNotFoundExceptionWhenMovieDoesNotExist() {
        MovieReqDto request = new MovieReqDto(
                "Gladiator Updated",
                "Updated description.",
                Genre.ACTION,
                160,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/gladiator-updated.jpg");

        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.updateMovie(99L, request))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie not found with ID: 99");

        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void updateMovieTranslatesDatabaseDuplicateViolationToMovieAlreadyExistsException() {
        MovieReqDto request = new MovieReqDto(
                "Gladiator Updated",
                "Updated description.",
                Genre.ACTION,
                160,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/gladiator-updated.jpg");

        Movie existingMovie = movieWithStatus("Gladiator", MovieStatus.COMING_SOON);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        doThrow(new DataIntegrityViolationException("Duplicate key"))
                .when(movieRepository)
                .save(existingMovie);

        assertThatThrownBy(() -> movieService.updateMovie(1L, request))
                .isInstanceOf(MovieAlreadyExistsException.class)
                .hasMessage("A movie with the same title, release date, and language already exists.");
    }

    @Test
    void deleteMovieSoftDeletesExistingMovie() {
        Movie movie = movieWithStatus("Interstellar", MovieStatus.COMING_SOON);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        movieService.deleteMovie(1L);

        assertThat(movie.isDeleted()).isTrue();

        verify(movieRepository).save(movie);
    }

    @Test
    void deleteMovieThrowsMovieNotFoundExceptionWhenMovieDoesNotExist() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.deleteMovie(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie not found with ID: 99");

        verify(movieRepository, never()).save(any(Movie.class));
    }
}
