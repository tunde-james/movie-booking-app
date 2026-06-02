package com.example.moviebookingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.moviebookingapp.dtos.booking.BookingReqDto;
import com.example.moviebookingapp.dtos.booking.BookingResDto;
import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.entity.Booking;
import com.example.moviebookingapp.enums.BookingStatus;
import com.example.moviebookingapp.enums.ShowStatus;
import com.example.moviebookingapp.exception.BookingNotFoundException;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
import com.example.moviebookingapp.exception.InsufficientShowCapacityException;
import com.example.moviebookingapp.exception.InvalidBookingRequestException;
import com.example.moviebookingapp.exception.ShowNotBookableException;
import com.example.moviebookingapp.exception.UserNotFoundException;
import com.example.moviebookingapp.service.BookingService;

@SuppressWarnings("null")
@WebMvcTest(BookingController.class)
@Import(GlobalExceptionHandler.class)
class BookingControllerApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @WithMockUser(roles = "USER")
    void createBookingReturnsConflictWhenShowIsNotBookable() throws Exception {

        BookingReqDto reqDto = new BookingReqDto(null, "Ada", "Lovelace", "ada@example.com", null, 10L, 2);

        when(bookingService.createBooking(any(BookingReqDto.class)))
                .thenThrow(new ShowNotBookableException("This show is sold out"));

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Show not bookable"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("This show is sold out"))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBookingReturnsConflictWhenShowCapacityIsInsufficient() throws Exception {

        BookingReqDto reqDto = new BookingReqDto(null, "Ada", "Lovelace", "ada@example.com", null, 10L, 5);

        when(bookingService.createBooking(any(BookingReqDto.class)))
                .thenThrow(new InsufficientShowCapacityException("Not enough seats available for this show"));

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Insufficient show capacity"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Not enough seats available for this show"))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBookingReturnsBadRequestWhenBookingRequestIsInvalid() throws Exception {

        BookingReqDto reqDto = new BookingReqDto(null, "Ada", "Lovelace", "ada@example.com", null, 10L, 2);

        when(bookingService.createBooking(any(BookingReqDto.class)))
                .thenThrow(new InvalidBookingRequestException("Ticket quantity must be greater than zero"));

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid booking request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Ticket quantity must be greater than zero"))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBookingReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        BookingReqDto reqDto = new BookingReqDto(99L, "Ada", "Lovelace", "ada@example.com", null, 10L, 2);

        when(bookingService.createBooking(any(BookingReqDto.class)))
                .thenThrow(new UserNotFoundException("User not found with ID: 99"));

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("User not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("User not found with ID: 99"))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBookingReturnsValidationErrorWhenGuestEmailIsBlank() throws Exception {
        String requestBody = """
            {
              "firstName": "Ada",
              "lastName": "Lovelace",
              "email": "",
              "showId": 10,
              "ticketQuantity": 2
            }
            """;

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')]").exists());

        verifyNoInteractions(bookingService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void confirmBookingReturnsOkAndConfirmedBooking() throws Exception {

        BookingResDto response = new BookingResDto(
                100L,
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                null,
                2,
                new BigDecimal("3500.00"),
                new BigDecimal("7000.00"),
                BookingStatus.CONFIRMED,
                null);

        when(bookingService.confirmBooking(100L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/confirm", 100L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.ticketQuantity").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).confirmBooking(100L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void confirmBookingReturnsNotFoundWhenBookingDoesNotExist() throws Exception {

        when(bookingService.confirmBooking(999L))
                .thenThrow(new BookingNotFoundException("Booking not found with ID: 999"));

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/confirm", 999L).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Booking not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Booking not found with ID: 999"))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings/999/confirm"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void confirmBookingReturnsBadRequestWhenBookingIsNotPending() throws Exception {

        when(bookingService.confirmBooking(100L))
                .thenThrow(new InvalidBookingRequestException("Only pending bookings can be confirmed"));

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/confirm", 100L).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid booking request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Only pending bookings can be confirmed"))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings/100/confirm"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void cancelBookingReturnsOkAndCancelledBooking() throws Exception {

        BookingResDto response = new BookingResDto(
                100L,
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                null,
                2,
                new BigDecimal("3500.00"),
                new BigDecimal("7000.00"),
                BookingStatus.CANCELLED,
                null);

        when(bookingService.cancelBooking(100L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", 100L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.ticketQuantity").value(2))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(bookingService).cancelBooking(100L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void cancelBookingReturnsNotFoundWhenBookingDoesNotExist() throws Exception {

        when(bookingService.cancelBooking(999L))
                .thenThrow(new BookingNotFoundException("Booking not found with ID: 999"));

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", 999L).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Booking not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Booking not found with ID: 999"))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings/999/cancel"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void cancelBookingReturnsBadRequestWhenBookingCannotBeCancelled() throws Exception {

        when(bookingService.cancelBooking(100L))
                .thenThrow(new InvalidBookingRequestException("Booking is already cancelled"));

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", 100L).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid booking request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Booking is already cancelled"))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings/100/cancel"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBookingByIdReturnsOkAndBooking() throws Exception {

        BookingResDto response = new BookingResDto(
                100L,
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                null,
                2,
                new BigDecimal("3500.00"),
                new BigDecimal("7000.00"),
                BookingStatus.CONFIRMED,
                null);

        when(bookingService.getBookingById(100L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/bookings/{bookingId}", 100L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.ticketQuantity").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).getBookingById(100L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBookingByIdReturnsNotFoundWhenBookingDoesNotExist() throws Exception {

        when(bookingService.getBookingById(999L))
                .thenThrow(new BookingNotFoundException("Booking not found with ID: 999"));

        mockMvc.perform(get("/api/v1/bookings/{bookingId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Booking not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Booking not found with ID: 999"))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings/999"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void confirmBookingReturnsConflictWhenConcurrentUpdateHappens() throws Exception {

        when(bookingService.confirmBooking(100L))
                .thenThrow(new ObjectOptimisticLockingFailureException(Booking.class, 100L));

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/confirm", 100L).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Concurrent update conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("The resource was changed by another request. Please try again."))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings/100/confirm"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void cancelBookingReturnsConflictWhenConcurrentUpdateHappens() throws Exception {
        
        when(bookingService.cancelBooking(100L))
                .thenThrow(new ObjectOptimisticLockingFailureException(Booking.class, 100L));

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", 100L).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Concurrent update conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("The resource was changed by another request. Please try again."))
                .andExpect(jsonPath("$.instance").value("/api/v1/bookings/100/cancel"));
    }
}
