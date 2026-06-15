package com.example.moviebookingapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.moviebookingapp.config.SecurityConfig;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
import com.example.moviebookingapp.service.AuditoriumService;
import com.example.moviebookingapp.service.AuthService;
import com.example.moviebookingapp.service.BookingService;
import com.example.moviebookingapp.service.CinemaService;
import com.example.moviebookingapp.service.MovieService;
import com.example.moviebookingapp.service.ShowService;

@SuppressWarnings("null")
@WebMvcTest
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SecurityEnforcementTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private CinemaService cinemaService;

    @MockitoBean
    private AuditoriumService auditoriumService;

    @MockitoBean
    private ShowService showService;

    @MockitoBean
    private BookingService bookingService;

    @Nested
    class PublicEndpoints {

        @Test
        void getMoviesIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(get("/api/v1/movies"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void getMovieByIdIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(get("/api/v1/movies/1"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void getShowsIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(get("/api/v1/shows"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void getShowByIdIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(get("/api/v1/shows/1"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void createBookingIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(post("/api/v1/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void getBookingByIdIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(get("/api/v1/bookings/1"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void confirmBookingIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(post("/api/v1/bookings/1/confirm").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void cancelBookingIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(post("/api/v1/bookings/1/cancel").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void registerIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void loginIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void getCinemasIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(get("/api/v1/cinemas"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void getCinemaByIdIsAccessibleWithoutAuth() throws Exception {

            mockMvc.perform(get("/api/v1/cinemas/1"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }
    }

    @Nested
    class UnauthenticatedRequestsAreRejected {

        @Test
        void createMovieRequiresAuthentication() throws Exception {

            mockMvc.perform(post("/api/v1/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void updateMovieRequiresAuthentication() throws Exception {

            mockMvc.perform(put("/api/v1/movies/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void deleteMovieRequiresAuthentication() throws Exception {

            mockMvc.perform(delete("/api/v1/movies/1")).andExpect(status().isUnauthorized());
        }

        @Test
        void createCinemaRequiresAuthentication() throws Exception {

            mockMvc.perform(post("/api/v1/cinemas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void updateCinemaRequiresAuthentication() throws Exception {

            mockMvc.perform(put("/api/v1/cinemas/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void deleteCinemaRequiresAuthentication() throws Exception {

            mockMvc.perform(delete("/api/v1/cinemas/1")).andExpect(status().isUnauthorized());
        }

        @Test
        void createAuditoriumRequiresAuthentication() throws Exception {

            mockMvc.perform(post("/api/v1/cinemas/1/auditoriums")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void updateAuditoriumRequiresAuthentication() throws Exception {

            mockMvc.perform(put("/api/v1/cinemas/1/auditoriums/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void deleteAuditoriumRequiresAuthentication() throws Exception {

            mockMvc.perform(delete("/api/v1/cinemas/1/auditoriums/1")).andExpect(status().isUnauthorized());
        }

        @Test
        void createShowRequiresAuthentication() throws Exception {

            mockMvc.perform(post("/api/v1/shows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void updateShowRequiresAuthentication() throws Exception {

            mockMvc.perform(put("/api/v1/shows/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void deleteShowRequiresAuthentication() throws Exception {

            mockMvc.perform(delete("/api/v1/shows/1")).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @WithMockUser(roles = "CUSTOMER")
    class CustomerRoleIsRejectedFromAdminEndpoints {

        @Test
        void createMovieIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(post("/api/v1/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void updateMovieIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(put("/api/v1/movies/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteMovieIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(delete("/api/v1/movies/1")).andExpect(status().isForbidden());
        }

        @Test
        void createCinemaIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(post("/api/v1/cinemas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void updateCinemaIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(put("/api/v1/cinemas/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteCinemaIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(delete("/api/v1/cinemas/1")).andExpect(status().isForbidden());
        }

        @Test
        void createAuditoriumIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(post("/api/v1/cinemas/1/auditoriums")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void updateAuditoriumIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(put("/api/v1/cinemas/1/auditoriums/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteAuditoriumIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(delete("/api/v1/cinemas/1/auditoriums/1")).andExpect(status().isForbidden());
        }

        @Test
        void createShowIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(post("/api/v1/shows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void updateShowIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(put("/api/v1/shows/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteShowIsForbiddenForCustomer() throws Exception {

            mockMvc.perform(delete("/api/v1/shows/1")).andExpect(status().isForbidden());
        }
    }
}
