package com.example.moviebookingapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.example.moviebookingapp.dtos.cinema.CinemaReqDto;
import com.example.moviebookingapp.dtos.cinema.CinemaResDto;
import com.example.moviebookingapp.exception.CinemaNotFoundException;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
import com.example.moviebookingapp.service.CinemaService;

@SuppressWarnings("null")
@WebMvcTest(CinemaController.class)
@Import(GlobalExceptionHandler.class)
class CinemaControllerApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CinemaService cinemaService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void addCinemaReturnsCreatedResourceAndLocationHeader() throws Exception {

        CinemaResDto createdCinema = new CinemaResDto(1L, "Filmhouse Lekki", "Admiralty Way, Lekki Phase 1", "Lagos");

        when(cinemaService.addCinema(any(CinemaReqDto.class))).thenReturn(createdCinema);

        String requestBody = """
            {
              "name": "Filmhouse Lekki",
              "address": "Admiralty Way, Lekki Phase 1",
              "city": "Lagos"
            }
            """;

        mockMvc.perform(post("/api/v1/cinemas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/cinemas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Filmhouse Lekki"))
                .andExpect(jsonPath("$.city").value("Lagos"));
    }

    @Test
    @WithMockUser
    void getCinemasReturnsOkAndCinemaList() throws Exception {

        CinemaResDto firstCinema = new CinemaResDto(1L, "Filmhouse Lekki", "Admiralty Way, Lekki Phase 1", "Lagos");

        CinemaResDto secondCinema = new CinemaResDto(2L, "Genesis Deluxe", "The Palms Mall", "Lagos");

        when(cinemaService.getCinemas()).thenReturn(List.of(firstCinema, secondCinema));

        mockMvc.perform(get("/api/v1/cinemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Filmhouse Lekki"))
                .andExpect(jsonPath("$[0].city").value("Lagos"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Genesis Deluxe"))
                .andExpect(jsonPath("$[1].city").value("Lagos"));
    }

    @Test
    @WithMockUser
    void getCinemaByIdReturnsOkAndCinemaDetails() throws Exception {

        CinemaResDto cinema = new CinemaResDto(1L, "Filmhouse Lekki", "Admiralty Way, Lekki Phase 1", "Lagos");

        when(cinemaService.getCinemaById(1L)).thenReturn(cinema);

        mockMvc.perform(get("/api/v1/cinemas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Filmhouse Lekki"))
                .andExpect(jsonPath("$.address").value("Admiralty Way, Lekki Phase 1"))
                .andExpect(jsonPath("$.city").value("Lagos"));
    }

    @Test
    @WithMockUser
    void getCinemaByIdReturnsProblemDetailsWhenCinemaDoesNotExist() throws Exception {

        when(cinemaService.getCinemaById(99L)).thenThrow(new CinemaNotFoundException("Cinema not found with ID: 99"));

        mockMvc.perform(get("/api/v1/cinemas/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/cinema-not-found"))
                .andExpect(jsonPath("$.title").value("Cinema not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Cinema not found with ID: 99"))
                .andExpect(jsonPath("$.instance").value("/api/v1/cinemas/99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCinemaReturnsOkAndUpdatedResource() throws Exception {

        CinemaResDto updatedCinema = new CinemaResDto(1L, "Filmhouse Lekki Updated", "Admiralty Way", "Lagos");

        when(cinemaService.updateCinema(any(Long.class), any(CinemaReqDto.class)))
                .thenReturn(updatedCinema);

        String requestBody = """
        {
          "name": "Filmhouse Lekki Updated",
          "address": "Admiralty Way",
          "city": "Lagos"
        }
        """;

        mockMvc.perform(put("/api/v1/cinemas/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Filmhouse Lekki Updated"))
                .andExpect(jsonPath("$.address").value("Admiralty Way"))
                .andExpect(jsonPath("$.city").value("Lagos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCinemaReturnsNoContent() throws Exception {

        doNothing().when(cinemaService).deleteCinema(1L);

        mockMvc.perform(delete("/api/v1/cinemas/1").with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cinemaService).deleteCinema(1L);
    }
}
