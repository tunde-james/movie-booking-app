package com.example.moviebookingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moviebookingapp.dtos.cinema.CinemaReqDto;
import com.example.moviebookingapp.dtos.cinema.CinemaResDto;
import com.example.moviebookingapp.entity.Cinema;
import com.example.moviebookingapp.mapper.CinemaMapper;
import com.example.moviebookingapp.repository.CinemaRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private CinemaService cinemaService;

    @Test
    void addCinemaSavesCinemaAndReturnsResponse() {

        CinemaReqDto request = new CinemaReqDto("  Filmhouse Lekki  ", "  Admiralty Way, Lekki Phase 1  ", "  Lagos  ");

        CinemaReqDto normalizedRequest = new CinemaReqDto("Filmhouse Lekki", "Admiralty Way, Lekki Phase 1", "Lagos");

        Cinema cinemaToSave = cinema("Filmhouse Lekki", "Admiralty Way, Lekki Phase 1", "Lagos");
        Cinema savedCinema = cinema("Filmhouse Lekki", "Admiralty Way, Lekki Phase 1", "Lagos");
        CinemaResDto response = new CinemaResDto(1L, "Filmhouse Lekki", "Admiralty Way, Lekki Phase 1", "Lagos");

        when(cinemaMapper.toEntity(normalizedRequest)).thenReturn(cinemaToSave);
        when(cinemaRepository.save(cinemaToSave)).thenReturn(savedCinema);
        when(cinemaMapper.toDto(savedCinema)).thenReturn(response);

        CinemaResDto result = cinemaService.addCinema(request);

        assertThat(result).isEqualTo(response);

        verify(cinemaMapper).toEntity(normalizedRequest);
        verify(cinemaRepository).save(cinemaToSave);
    }

    private Cinema cinema(String name, String address, String city) {

        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        cinema.setCity(city);
        return cinema;
    }
}
