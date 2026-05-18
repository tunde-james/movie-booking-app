package com.example.moviebookingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
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

    // @Test
    // @WithMockUser(roles = "ADMIN")
    // void updateMovieReturnsOkAndUpdatedResource() throws Exception {
    //     MovieResDto updatedMovie = new MovieResDto(
    //             1L,
    //             "Gladiator Updated",
    //             "Updated description.",
    //             Genre.ACTION,
    //             160,
    //             LocalDate.of(2026, 6, 1),
    //             Language.ENGLISH,
    //             MovieRating.PG_13,
    //             MovieStatus.COMING_SOON,
    //             "https://example.com/gladiator-updated.jpg");

    //     when(movieService.updateMovie(any(Long.class), any(MovieReqDto.class))).thenReturn(updatedMovie);

    //     String requestBody = """
    //         {
    //           "title": "Gladiator Updated",
    //           "description": "Updated description.",
    //           "genre": "ACTION",
    //           "durationInMinutes": 160,
    //           "releaseDate": "2026-06-01",
    //           "language": "ENGLISH",
    //           "rating": "PG_13",
    //           "movieStatus": "COMING_SOON",
    //           "posterUrl": "https://example.com/gladiator-updated.jpg"
    //         }
    //         """;

    //     mockMvc.perform(put("/api/v1/movies/1")
    //                     .with(csrf())
    //                     .contentType(MediaType.APPLICATION_JSON)
    //                     .content(requestBody))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.id").value(1))
    //             .andExpect(jsonPath("$.title").value("Gladiator Updated"))
    //             .andExpect(jsonPath("$.movieStatus").value("COMING_SOON"));
    // }
}
