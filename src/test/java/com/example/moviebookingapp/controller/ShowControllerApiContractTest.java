package com.example.moviebookingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;

import com.example.moviebookingapp.dtos.show.ShowReqDto;
import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.enums.ShowStatus;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
import com.example.moviebookingapp.exception.InvalidShowScheduleException;
import com.example.moviebookingapp.exception.ShowScheduleConflictException;
import com.example.moviebookingapp.service.ShowService;

@SuppressWarnings("null")
@WebMvcTest(ShowController.class)
@Import(GlobalExceptionHandler.class)
class ShowControllerApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShowService showService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void addShowReturnsCreatedResourceAndLocationHeader() throws Exception {

        ShowResDto createdShow = new ShowResDto(
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

        when(showService.addShow(any(ShowReqDto.class))).thenReturn(createdShow);

        String requestBody = """
            {
              "movieId": 100,
              "auditoriumId": 20,
              "startTime": "2026-06-01T18:30:00+01:00",
              "endTime": "2026-06-01T20:45:00+01:00",
              "pricePerTicket": 3500.00
            }
            """;

        mockMvc.perform(post("/api/v1/shows")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/shows/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.movieId").value(100))
                .andExpect(jsonPath("$.movieTitle").value("Gladiator"))
                .andExpect(jsonPath("$.cinemaId").value(10))
                .andExpect(jsonPath("$.cinemaName").value("Filmhouse Lekki"))
                .andExpect(jsonPath("$.auditoriumId").value(20))
                .andExpect(jsonPath("$.auditoriumName").value("Screen 1"))
                .andExpect(jsonPath("$.startTime").value("2026-06-01T18:30:00+01:00"))
                .andExpect(jsonPath("$.endTime").value("2026-06-01T20:45:00+01:00"))
                .andExpect(jsonPath("$.totalCapacity").value(120))
                .andExpect(jsonPath("$.availableCapacity").value(120))
                .andExpect(jsonPath("$.pricePerTicket").value(3500.00))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addShowReturnsProblemDetailsWhenScheduleIsInvalid() throws Exception {

        when(showService.addShow(any(ShowReqDto.class)))
                .thenThrow(new InvalidShowScheduleException("Show end time must be after start time"));

        String requestBody = """
        {
          "movieId": 100,
          "auditoriumId": 20,
          "startTime": "2026-06-01T20:45:00+01:00",
          "endTime": "2026-06-01T18:30:00+01:00",
          "pricePerTicket": 3500.00
        }
        """;

        mockMvc.perform(post("/api/v1/shows")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/invalid-show-schedule"))
                .andExpect(jsonPath("$.title").value("Invalid show schedule"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Show end time must be after start time"))
                .andExpect(jsonPath("$.instance").value("/api/v1/shows"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addShowReturnsProblemDetailsWhenScheduleConflicts() throws Exception {

        when(showService.addShow(any(ShowReqDto.class)))
                .thenThrow(new ShowScheduleConflictException(
                        "Auditorium already has a scheduled show in this time window"));

        String requestBody = """
        {
          "movieId": 100,
          "auditoriumId": 20,
          "startTime": "2026-06-01T20:10:00+01:00",
          "endTime": "2026-06-01T22:00:00+01:00",
          "pricePerTicket": 3500.00
        }
        """;

        mockMvc.perform(post("/api/v1/shows")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/show-schedule-conflict"))
                .andExpect(jsonPath("$.title").value("Show schedule conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Auditorium already has a scheduled show in this time window"))
                .andExpect(jsonPath("$.instance").value("/api/v1/shows"));
    }
}
