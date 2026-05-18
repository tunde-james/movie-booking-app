package com.example.moviebookingapp.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;

import com.example.moviebookingapp.dtos.movie.MovieReqDto;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
import com.example.moviebookingapp.exception.MovieAlreadyExistsException;
import com.example.moviebookingapp.service.MovieService;

@SuppressWarnings({"null"})
@WebMvcTest(MovieController.class)
@Import(GlobalExceptionHandler.class)
class MovieControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMovieReturnsProblemDetailsWhenRequestIsInvalid() throws Exception {
        String invalidRequest = """
                {
                  "title": "",
                  "description": "A movie with missing required fields"
                }
                """;

        mockMvc.perform(post("/api/v1/movies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/validation-error"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
                .andExpect(jsonPath("$.instance").value("/api/v1/movies"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("Title is required")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMovieReturnsProblemDetailsWhenMovieAlreadyExists() throws Exception {
        String validRequest = """
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

        when(movieService.addMovie(any(MovieReqDto.class)))
                .thenThrow(new MovieAlreadyExistsException(
                        "A movie with the same title, release date, and language already exists."));

        mockMvc.perform(post("/api/v1/movies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/movie-already-exists"))
                .andExpect(jsonPath("$.title").value("Movie already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("A movie with the same title, release date, and language already exists."))
                .andExpect(jsonPath("$.instance").value("/api/v1/movies"));
    }
}
