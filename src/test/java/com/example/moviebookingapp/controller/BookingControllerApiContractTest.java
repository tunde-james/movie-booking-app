package com.example.moviebookingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.example.moviebookingapp.dtos.booking.BookingReqDto;
import com.example.moviebookingapp.dtos.booking.BookingResDto;
import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.enums.BookingStatus;
import com.example.moviebookingapp.enums.ShowStatus;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
import com.example.moviebookingapp.service.BookingService;

@SuppressWarnings("null")
@WebMvcTest(BookingController.class)
@Import(GlobalExceptionHandler.class)
class BookingControllerApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    @WithMockUser
    void createBookingReturnsCreatedResourceAndLocationHeader() throws Exception {

        ShowResDto show = new ShowResDto(
                10L,
                100L,
                "Gladiator",
                20L,
                "Filmhouse Lekki",
                30L,
                "Screen 1",
                OffsetDateTime.parse("2026-06-01T18:30:00+01:00"),
                OffsetDateTime.parse("2026-06-01T20:45:00+01:00"),
                120,
                50,
                new BigDecimal("3500.00"),
                ShowStatus.SCHEDULED);

        BookingResDto createdBooking = new BookingResDto(
                99L,
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                show,
                2,
                new BigDecimal("3500.00"),
                new BigDecimal("7000.00"),
                BookingStatus.PENDING,
                null);

        when(bookingService.createBooking(any(BookingReqDto.class))).thenReturn(createdBooking);

        String requestBody = """
        {
          "firstName": "Ada",
          "lastName": "Lovelace",
          "email": "ada@example.com",
          "showId": 10,
          "ticketQuantity": 2
        }
        """;

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/bookings/99"))
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.show.id").value(10))
                .andExpect(jsonPath("$.ticketQuantity").value(2))
                .andExpect(jsonPath("$.unitPrice").value(3500.00))
                .andExpect(jsonPath("$.totalPrice").value(7000.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
