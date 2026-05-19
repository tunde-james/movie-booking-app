package com.example.moviebookingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;

import com.example.moviebookingapp.dtos.movie.MovieReqDto;
import com.example.moviebookingapp.dtos.movie.MovieResDto;
import com.example.moviebookingapp.dtos.movie.MovieSearchCriteria;
import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
import com.example.moviebookingapp.exception.MovieNotFoundException;
import com.example.moviebookingapp.service.MovieService;

@SuppressWarnings({"null"})
@WebMvcTest(controllers = MovieController.class)
@Import(GlobalExceptionHandler.class)
class MovieControllerApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMovieReturnsCreatedResourceAndLocationHeader() throws Exception {

        MovieResDto createdMovie = new MovieResDto(
                1L,
                "Gladiator",
                "A historical action drama.",
                Genre.ACTION,
                155,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/gladiator.jpg");

        when(movieService.addMovie(any(MovieReqDto.class))).thenReturn(createdMovie);

        String requestBody = """
                {
                  "title": "Gladiator",
                  "description": "A historical action drama.",
                  "genre": "ACTION",
                  "durationInMinutes": 155,
                  "releaseDate": "2026-06-01",
                  "language": "ENGLISH",
                  "rating": "PG_13",
                  "movieStatus": "COMING_SOON",
                  "posterUrl": "https://example.com/gladiator.jpg"
                }
                """;

        mockMvc.perform(post("/api/v1/movies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/movies/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Gladiator"))
                .andExpect(jsonPath("$.movieStatus").value("COMING_SOON"));
    }

    @Test
    @WithMockUser
    void getPublicMoviesReturnsOkAndMovieList() throws Exception {

        MovieResDto comingSoonMovie = new MovieResDto(
                1L,
                "Interstellar",
                "A space exploration movie.",
                Genre.SCI_FI,
                169,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/interstellar.jpg");

        MovieResDto nowShowingMovie = new MovieResDto(
                2L,
                "Inception",
                "A dream heist movie.",
                Genre.ACTION,
                148,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.NOW_SHOWING,
                "https://example.com/inception.jpg");

        when(movieService.searchMovies(any(MovieSearchCriteria.class)))
                .thenReturn(List.of(comingSoonMovie, nowShowingMovie));

        mockMvc.perform(get("/api/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Interstellar"))
                .andExpect(jsonPath("$[0].movieStatus").value("COMING_SOON"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Inception"))
                .andExpect(jsonPath("$[1].movieStatus").value("NOW_SHOWING"));
    }

    @Test
    @WithMockUser
    void getMovieByIdReturnsOkAndMovieDetails() throws Exception {
        MovieResDto movie = new MovieResDto(
                1L,
                "Interstellar",
                "A space exploration movie.",
                Genre.SCI_FI,
                169,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/interstellar.jpg");

        when(movieService.getMovieById(1L)).thenReturn(movie);

        mockMvc.perform(get("/api/v1/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Interstellar"))
                .andExpect(jsonPath("$.movieStatus").value("COMING_SOON"));
    }

    @Test
    @WithMockUser
    void getMovieByIdReturnsProblemDetailsWhenMovieDoesNotExist() throws Exception {
        when(movieService.getMovieById(99L)).thenThrow(new MovieNotFoundException("Movie not found with ID: 99"));

        mockMvc.perform(get("/api/v1/movies/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/movie-not-found"))
                .andExpect(jsonPath("$.title").value("Movie not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Movie not found with ID: 99"))
                .andExpect(jsonPath("$.instance").value("/api/v1/movies/99"));
    }

    @Test
    @WithMockUser
    void getMoviesSupportsOptionalFilters() throws Exception {
        MovieResDto movie = new MovieResDto(
                1L,
                "Interstellar",
                "A space exploration movie.",
                Genre.SCI_FI,
                169,
                LocalDate.of(2026, 6, 1),
                Language.ENGLISH,
                MovieRating.PG_13,
                MovieStatus.COMING_SOON,
                "https://example.com/interstellar.jpg");

        when(movieService.searchMovies(any(MovieSearchCriteria.class))).thenReturn(List.of(movie));

        mockMvc.perform(get("/api/v1/movies")
                        .param("title", "Interstellar")
                        .param("genre", "SCI_FI")
                        .param("language", "ENGLISH")
                        .param("status", "COMING_SOON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Interstellar"))
                .andExpect(jsonPath("$[0].genre").value("SCI_FI"))
                .andExpect(jsonPath("$[0].language").value("ENGLISH"))
                .andExpect(jsonPath("$[0].movieStatus").value("COMING_SOON"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMovieReturnsNoContent() throws Exception {
        doNothing().when(movieService).deleteMovie(1L);

        mockMvc.perform(delete("/api/v1/movies/1").with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(movieService).deleteMovie(1L);
    }
}
