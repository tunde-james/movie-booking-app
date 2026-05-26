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

import com.example.moviebookingapp.dtos.auditorium.AuditoriumReqDto;
import com.example.moviebookingapp.dtos.auditorium.AuditoriumResDto;
import com.example.moviebookingapp.enums.AuditoriumType;
import com.example.moviebookingapp.exception.AuditoriumAlreadyExistsException;
import com.example.moviebookingapp.exception.AuditoriumNotFoundException;
import com.example.moviebookingapp.exception.CinemaNotFoundException;
import com.example.moviebookingapp.exception.GlobalExceptionHandler;
import com.example.moviebookingapp.service.AuditoriumService;

@SuppressWarnings("null")
@WebMvcTest(AuditoriumController.class)
@Import(GlobalExceptionHandler.class)
class AuditoriumControllerApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditoriumService auditoriumService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void addAuditoriumReturnsCreatedResourceAndLocationHeader() throws Exception {

        AuditoriumResDto createdAuditorium = new AuditoriumResDto(1L, 10L, "Screen 1", AuditoriumType.STANDARD, 120);

        when(auditoriumService.addAuditorium(any(Long.class), any(AuditoriumReqDto.class)))
                .thenReturn(createdAuditorium);

        String requestBody = """
            {
              "name": "Screen 1",
              "type": "STANDARD",
              "capacity": 120
            }
            """;

        mockMvc.perform(post("/api/v1/cinemas/10/auditoriums")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/cinemas/10/auditoriums/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cinemaId").value(10))
                .andExpect(jsonPath("$.name").value("Screen 1"))
                .andExpect(jsonPath("$.type").value("STANDARD"))
                .andExpect(jsonPath("$.capacity").value(120));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addAuditoriumReturnsProblemDetailsWhenCinemaDoesNotExist() throws Exception {

        when(auditoriumService.addAuditorium(any(Long.class), any(AuditoriumReqDto.class)))
                .thenThrow(new CinemaNotFoundException("Cinema not found with ID: 99"));

        String requestBody = """
        {
          "name": "Screen 1",
          "type": "STANDARD",
          "capacity": 120
        }
        """;

        mockMvc.perform(post("/api/v1/cinemas/99/auditoriums")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/cinema-not-found"))
                .andExpect(jsonPath("$.title").value("Cinema not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Cinema not found with ID: 99"))
                .andExpect(jsonPath("$.instance").value("/api/v1/cinemas/99/auditoriums"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addAuditoriumReturnsProblemDetailsWhenAuditoriumAlreadyExists() throws Exception {

        when(auditoriumService.addAuditorium(any(Long.class), any(AuditoriumReqDto.class)))
                .thenThrow(new AuditoriumAlreadyExistsException(
                        "An auditorium with the same name already exists in this cinema."));

        String requestBody = """
        {
          "name": "Screen 1",
          "type": "STANDARD",
          "capacity": 120
        }
        """;

        mockMvc.perform(post("/api/v1/cinemas/10/auditoriums")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/auditorium-already-exists"))
                .andExpect(jsonPath("$.title").value("Auditorium already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(
                        jsonPath("$.detail").value("An auditorium with the same name already exists in this cinema."))
                .andExpect(jsonPath("$.instance").value("/api/v1/cinemas/10/auditoriums"));
    }

    @Test
    @WithMockUser
    void getAuditoriumsForCinemaReturnsOkAndAuditoriumList() throws Exception {

        AuditoriumResDto firstAuditorium = new AuditoriumResDto(1L, 10L, "Screen 1", AuditoriumType.STANDARD, 120);

        AuditoriumResDto secondAuditorium = new AuditoriumResDto(2L, 10L, "IMAX Hall", AuditoriumType.IMAX, 250);

        when(auditoriumService.getAuditoriumsByCinema(10L)).thenReturn(List.of(firstAuditorium, secondAuditorium));

        mockMvc.perform(get("/api/v1/cinemas/10/auditoriums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cinemaId").value(10))
                .andExpect(jsonPath("$[0].name").value("Screen 1"))
                .andExpect(jsonPath("$[0].type").value("STANDARD"))
                .andExpect(jsonPath("$[0].capacity").value(120))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].cinemaId").value(10))
                .andExpect(jsonPath("$[1].name").value("IMAX Hall"))
                .andExpect(jsonPath("$[1].type").value("IMAX"))
                .andExpect(jsonPath("$[1].capacity").value(250));
    }

    @Test
    @WithMockUser
    void getAuditoriumByIdReturnsOkAndAuditoriumDetails() throws Exception {

        AuditoriumResDto auditorium = new AuditoriumResDto(1L, 10L, "Screen 1", AuditoriumType.STANDARD, 120);

        when(auditoriumService.getAuditoriumById(10L, 1L)).thenReturn(auditorium);

        mockMvc.perform(get("/api/v1/cinemas/10/auditoriums/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cinemaId").value(10))
                .andExpect(jsonPath("$.name").value("Screen 1"))
                .andExpect(jsonPath("$.type").value("STANDARD"))
                .andExpect(jsonPath("$.capacity").value(120));
    }

    @Test
    @WithMockUser
    void getAuditoriumByIdReturnsProblemDetailsWhenAuditoriumDoesNotExist() throws Exception {

        when(auditoriumService.getAuditoriumById(10L, 99L))
                .thenThrow(new AuditoriumNotFoundException("Auditorium not found with ID: 99"));

        mockMvc.perform(get("/api/v1/cinemas/10/auditoriums/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/auditorium-not-found"))
                .andExpect(jsonPath("$.title").value("Auditorium not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Auditorium not found with ID: 99"))
                .andExpect(jsonPath("$.instance").value("/api/v1/cinemas/10/auditoriums/99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAuditoriumReturnsOkAndUpdatedResource() throws Exception {

        AuditoriumResDto updatedAuditorium =
                new AuditoriumResDto(1L, 10L, "Screen 1 Updated", AuditoriumType.IMAX, 200);

        when(auditoriumService.updateAuditorium(any(Long.class), any(Long.class), any(AuditoriumReqDto.class)))
                .thenReturn(updatedAuditorium);

        String requestBody = """
        {
          "name": "Screen 1 Updated",
          "type": "IMAX",
          "capacity": 200
        }
        """;

        mockMvc.perform(put("/api/v1/cinemas/10/auditoriums/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cinemaId").value(10))
                .andExpect(jsonPath("$.name").value("Screen 1 Updated"))
                .andExpect(jsonPath("$.type").value("IMAX"))
                .andExpect(jsonPath("$.capacity").value(200));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAuditoriumReturnsProblemDetailsWhenAuditoriumDoesNotExist() throws Exception {

        when(auditoriumService.updateAuditorium(any(Long.class), any(Long.class), any(AuditoriumReqDto.class)))
                .thenThrow(new AuditoriumNotFoundException("Auditorium not found with ID: 99"));

        String requestBody = """
        {
          "name": "Screen 1",
          "type": "STANDARD",
          "capacity": 120
        }
        """;

        mockMvc.perform(put("/api/v1/cinemas/10/auditoriums/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/auditorium-not-found"))
                .andExpect(jsonPath("$.title").value("Auditorium not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Auditorium not found with ID: 99"))
                .andExpect(jsonPath("$.instance").value("/api/v1/cinemas/10/auditoriums/99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAuditoriumReturnsProblemDetailsWhenAuditoriumNameAlreadyExists() throws Exception {

        when(auditoriumService.updateAuditorium(any(Long.class), any(Long.class), any(AuditoriumReqDto.class)))
                .thenThrow(new AuditoriumAlreadyExistsException(
                        "An auditorium with the same name already exists in this cinema."));

        String requestBody = """
        {
          "name": "IMAX Hall",
          "type": "IMAX",
          "capacity": 250
        }
        """;

        mockMvc.perform(put("/api/v1/cinemas/10/auditoriums/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://moviebookingapp/problems/auditorium-already-exists"))
                .andExpect(jsonPath("$.title").value("Auditorium already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(
                        jsonPath("$.detail").value("An auditorium with the same name already exists in this cinema."))
                .andExpect(jsonPath("$.instance").value("/api/v1/cinemas/10/auditoriums/1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAuditoriumReturnsNoContent() throws Exception {

        doNothing().when(auditoriumService).deleteAuditorium(10L, 1L);

        mockMvc.perform(delete("/api/v1/cinemas/10/auditoriums/1").with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(auditoriumService).deleteAuditorium(10L, 1L);
    }
}
