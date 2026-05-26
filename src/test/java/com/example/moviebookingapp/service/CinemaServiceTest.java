package com.example.moviebookingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moviebookingapp.dtos.cinema.CinemaReqDto;
import com.example.moviebookingapp.dtos.cinema.CinemaResDto;
import com.example.moviebookingapp.entity.Cinema;
import com.example.moviebookingapp.exception.CinemaAlreadyExistsException;
import com.example.moviebookingapp.exception.CinemaNotFoundException;
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
    void getCinemasReturnsNonDeletedCinemas() {

        Cinema cinema = cinema("Filmhouse Lekki", "Admiralty Way", "Lagos");
        CinemaResDto response = new CinemaResDto(1L, "Filmhouse Lekki", "Admiralty Way", "Lagos");

        when(cinemaRepository.findAllByDeletedFalse()).thenReturn(List.of(cinema));
        when(cinemaMapper.toDtoList(List.of(cinema))).thenReturn(List.of(response));

        List<CinemaResDto> result = cinemaService.getCinemas();

        assertThat(result).containsExactly(response);

        verify(cinemaRepository).findAllByDeletedFalse();
    }

    @Test
    void getCinemaByIdReturnsNonDeletedCinema() {

        Cinema cinema = cinema("Filmhouse Lekki", "Admiralty Way", "Lagos");
        CinemaResDto response = new CinemaResDto(1L, "Filmhouse Lekki", "Admiralty Way", "Lagos");

        when(cinemaRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(cinema));
        when(cinemaMapper.toDto(cinema)).thenReturn(response);

        CinemaResDto result = cinemaService.getCinemaById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getCinemaByIdReturnsNotFoundWhenCinemaIsMissingOrDeleted() {

        when(cinemaRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.getCinemaById(99L))
                .isInstanceOf(CinemaNotFoundException.class)
                .hasMessage("Cinema not found with ID: 99");
    }

    @Test
    void addCinemaSavesCinemaAndReturnsResponse() {

        CinemaReqDto request = new CinemaReqDto("  Filmhouse Lekki  ", "  Admiralty Way  ", "  Lagos  ");
        CinemaReqDto normalizedRequest = new CinemaReqDto("Filmhouse Lekki", "Admiralty Way", "Lagos");

        Cinema cinemaToSave = cinema("Filmhouse Lekki", "Admiralty Way", "Lagos");
        Cinema savedCinema = cinema("Filmhouse Lekki", "Admiralty Way", "Lagos");
        CinemaResDto response = new CinemaResDto(1L, "Filmhouse Lekki", "Admiralty Way", "Lagos");

        when(cinemaMapper.toEntity(normalizedRequest)).thenReturn(cinemaToSave);
        when(cinemaRepository.save(cinemaToSave)).thenReturn(savedCinema);
        when(cinemaMapper.toDto(savedCinema)).thenReturn(response);

        CinemaResDto result = cinemaService.addCinema(request);

        assertThat(result).isEqualTo(response);
        verify(cinemaRepository).existsByNameIgnoreCase("Filmhouse Lekki");
        verify(cinemaMapper).toEntity(normalizedRequest);
    }

    @Test
    void addCinemaRejectsDuplicateName() {

        CinemaReqDto request = new CinemaReqDto(" Filmhouse Lekki ", "Address 2", "Abuja");

        when(cinemaRepository.existsByNameIgnoreCase("Filmhouse Lekki")).thenReturn(true);

        assertThatThrownBy(() -> cinemaService.addCinema(request))
                .isInstanceOf(CinemaAlreadyExistsException.class)
                .hasMessage("A cinema with the same name already exists.");

        verify(cinemaMapper, never()).toEntity(any(CinemaReqDto.class));
        verify(cinemaRepository, never()).save(any(Cinema.class));
    }

    @Test
    void updateCinemaRejectsDuplicateNameOwnedByAnotherCinema() {

        Cinema existingCinema = cinema("Genesis Deluxe", "The Palms Mall", "Lagos");
        CinemaReqDto request = new CinemaReqDto(" Filmhouse Lekki ", "Another Address", "Abuja");

        when(cinemaRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingCinema));
        when(cinemaRepository.existsByNameIgnoreCaseAndIdNot("Filmhouse Lekki", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> cinemaService.updateCinema(1L, request))
                .isInstanceOf(CinemaAlreadyExistsException.class)
                .hasMessage("A cinema with the same name already exists.");

        verify(cinemaMapper, never()).updateEntityFromDto(any(CinemaReqDto.class), any(Cinema.class));
        verify(cinemaRepository, never()).save(any(Cinema.class));
    }

    @Test
    void updateCinemaReturnsNotFoundBeforeDuplicateCheckWhenCinemaDoesNotExist() {

        CinemaReqDto request = new CinemaReqDto("Filmhouse Lekki", "Address", "Lagos");

        when(cinemaRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.updateCinema(99L, request))
                .isInstanceOf(CinemaNotFoundException.class)
                .hasMessage("Cinema not found with ID: 99");

        verify(cinemaRepository, never()).existsByNameIgnoreCaseAndIdNot(anyString(), anyLong());
    }

    private Cinema cinema(String name, String address, String city) {

        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        cinema.setCity(city);
        return cinema;
    }

    @Test
    void deleteCinemaSoftDeletesExistingCinema() {

        Cinema cinema = cinema("Filmhouse Lekki", "Admiralty Way", "Lagos");

        when(cinemaRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(cinema));
        when(cinemaRepository.save(cinema)).thenReturn(cinema);

        cinemaService.deleteCinema(1L);

        assertThat(cinema.isDeleted()).isTrue();

        verify(cinemaRepository).save(cinema);
    }
}
