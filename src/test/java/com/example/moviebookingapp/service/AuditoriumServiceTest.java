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

import com.example.moviebookingapp.dtos.auditorium.AuditoriumReqDto;
import com.example.moviebookingapp.dtos.auditorium.AuditoriumResDto;
import com.example.moviebookingapp.entity.Auditorium;
import com.example.moviebookingapp.entity.Cinema;
import com.example.moviebookingapp.enums.AuditoriumType;
import com.example.moviebookingapp.exception.AuditoriumAlreadyExistsException;
import com.example.moviebookingapp.exception.AuditoriumNotFoundException;
import com.example.moviebookingapp.exception.CinemaNotFoundException;
import com.example.moviebookingapp.mapper.AuditoriumMapper;
import com.example.moviebookingapp.repository.AuditoriumRepository;
import com.example.moviebookingapp.repository.CinemaRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuditoriumServiceTest {

    @Mock
    private AuditoriumRepository auditoriumRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private AuditoriumMapper auditoriumMapper;

    @InjectMocks
    private AuditoriumService auditoriumService;

    @Test
    void addAuditoriumSavesAuditoriumForExistingCinemaAndReturnsResponse() {

        Cinema cinema = new Cinema();
        AuditoriumReqDto request = new AuditoriumReqDto("  Screen 1  ", AuditoriumType.STANDARD, 120);
        AuditoriumReqDto normalizedRequest = new AuditoriumReqDto("Screen 1", AuditoriumType.STANDARD, 120);

        Auditorium auditoriumToSave = auditorium("Screen 1", AuditoriumType.STANDARD, 120);
        Auditorium savedAuditorium = auditorium("Screen 1", AuditoriumType.STANDARD, 120);
        AuditoriumResDto response = new AuditoriumResDto(1L, 10L, "Screen 1", AuditoriumType.STANDARD, 120);

        when(cinemaRepository.findById(10L)).thenReturn(Optional.of(cinema));
        when(auditoriumMapper.toEntity(normalizedRequest)).thenReturn(auditoriumToSave);
        when(auditoriumRepository.save(auditoriumToSave)).thenReturn(savedAuditorium);
        when(auditoriumMapper.toDto(savedAuditorium)).thenReturn(response);

        AuditoriumResDto result = auditoriumService.addAuditorium(10L, request);

        assertThat(result).isEqualTo(response);
        assertThat(auditoriumToSave.getCinema()).isEqualTo(cinema);

        verify(cinemaRepository).findById(10L);
        verify(auditoriumMapper).toEntity(normalizedRequest);
        verify(auditoriumRepository).save(auditoriumToSave);
    }

    @Test
    void addAuditoriumRejectsDuplicateNameWithinSameCinema() {

        Cinema cinema = new Cinema();
        AuditoriumReqDto request = new AuditoriumReqDto(" Screen 1 ", AuditoriumType.STANDARD, 120);

        when(cinemaRepository.findById(10L)).thenReturn(Optional.of(cinema));
        when(auditoriumRepository.existsByCinemaIdAndNameIgnoreCase(10L, "Screen 1"))
                .thenReturn(true);

        assertThatThrownBy(() -> auditoriumService.addAuditorium(10L, request))
                .isInstanceOf(AuditoriumAlreadyExistsException.class)
                .hasMessage("An auditorium with the same name already exists in this cinema.");

        verify(auditoriumMapper, never()).toEntity(any(AuditoriumReqDto.class));
        verify(auditoriumRepository, never()).save(any(Auditorium.class));
    }

    @Test
    void addAuditoriumReturnsNotFoundWhenCinemaDoesNotExist() {

        AuditoriumReqDto request = new AuditoriumReqDto("Screen 1", AuditoriumType.STANDARD, 120);

        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditoriumService.addAuditorium(99L, request))
                .isInstanceOf(CinemaNotFoundException.class)
                .hasMessage("Cinema not found with ID: 99");

        verify(auditoriumRepository, never()).existsByCinemaIdAndNameIgnoreCase(anyLong(), anyString());
        verify(auditoriumMapper, never()).toEntity(any(AuditoriumReqDto.class));
    }

    @Test
    void getAuditoriumsByCinemaReturnsAuditoriumsForExistingCinema() {

        Auditorium firstAuditorium = auditorium("Screen 1", AuditoriumType.STANDARD, 120);
        Auditorium secondAuditorium = auditorium("IMAX Hall", AuditoriumType.IMAX, 250);

        AuditoriumResDto firstResponse = new AuditoriumResDto(1L, 10L, "Screen 1", AuditoriumType.STANDARD, 120);

        AuditoriumResDto secondResponse = new AuditoriumResDto(2L, 10L, "IMAX Hall", AuditoriumType.IMAX, 250);

        when(cinemaRepository.existsById(10L)).thenReturn(true);
        when(auditoriumRepository.findByCinemaId(10L)).thenReturn(List.of(firstAuditorium, secondAuditorium));
        when(auditoriumMapper.toDtoList(List.of(firstAuditorium, secondAuditorium)))
                .thenReturn(List.of(firstResponse, secondResponse));

        List<AuditoriumResDto> result = auditoriumService.getAuditoriumsByCinema(10L);

        assertThat(result).containsExactly(firstResponse, secondResponse);

        verify(cinemaRepository).existsById(10L);
        verify(auditoriumRepository).findByCinemaId(10L);
    }

    @Test
    void getAuditoriumsByCinemaReturnsNotFoundWhenCinemaDoesNotExist() {

        when(cinemaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> auditoriumService.getAuditoriumsByCinema(99L))
                .isInstanceOf(CinemaNotFoundException.class)
                .hasMessage("Cinema not found with ID: 99");

        verify(auditoriumRepository, never()).findByCinemaId(99L);
    }

    @Test
    void getAuditoriumByIdReturnsAuditoriumWhenItBelongsToCinema() {

        Auditorium auditorium = auditorium("Screen 1", AuditoriumType.STANDARD, 120);
        AuditoriumResDto response = new AuditoriumResDto(1L, 10L, "Screen 1", AuditoriumType.STANDARD, 120);

        when(cinemaRepository.existsById(10L)).thenReturn(true);
        when(auditoriumRepository.findByCinemaIdAndId(10L, 1L)).thenReturn(Optional.of(auditorium));
        when(auditoriumMapper.toDto(auditorium)).thenReturn(response);

        AuditoriumResDto result = auditoriumService.getAuditoriumById(10L, 1L);

        assertThat(result).isEqualTo(response);
        verify(auditoriumRepository).findByCinemaIdAndId(10L, 1L);
    }

    @Test
    void getAuditoriumByIdReturnsNotFoundWhenAuditoriumDoesNotBelongToCinema() {

        when(cinemaRepository.existsById(10L)).thenReturn(true);
        when(auditoriumRepository.findByCinemaIdAndId(10L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditoriumService.getAuditoriumById(10L, 99L))
                .isInstanceOf(AuditoriumNotFoundException.class)
                .hasMessage("Auditorium not found with ID: 99");

        verify(auditoriumRepository).findByCinemaIdAndId(10L, 99L);
    }

    @Test
    void updateAuditoriumUpdatesExistingAuditoriumAndReturnsResponse() {

        Auditorium auditorium = auditorium("Screen 1", AuditoriumType.STANDARD, 120);

        AuditoriumReqDto request = new AuditoriumReqDto("  IMAX Hall  ", AuditoriumType.IMAX, 250);

        AuditoriumReqDto normalizedRequest = new AuditoriumReqDto("IMAX Hall", AuditoriumType.IMAX, 250);

        AuditoriumResDto response = new AuditoriumResDto(1L, 10L, "IMAX Hall", AuditoriumType.IMAX, 250);

        when(cinemaRepository.existsById(10L)).thenReturn(true);
        when(auditoriumRepository.findByCinemaIdAndId(10L, 1L)).thenReturn(Optional.of(auditorium));
        when(auditoriumRepository.existsByCinemaIdAndNameIgnoreCaseAndIdNot(10L, "IMAX Hall", 1L))
                .thenReturn(false);
        when(auditoriumRepository.save(auditorium)).thenReturn(auditorium);
        when(auditoriumMapper.toDto(auditorium)).thenReturn(response);

        AuditoriumResDto result = auditoriumService.updateAuditorium(10L, 1L, request);

        assertThat(result).isEqualTo(response);

        verify(auditoriumMapper).updateEntityFromDto(normalizedRequest, auditorium);
        verify(auditoriumRepository).save(auditorium);
    }

    @Test
    void updateAuditoriumReturnsNotFoundWhenCinemaDoesNotExist() {

        AuditoriumReqDto request = new AuditoriumReqDto("Screen 1", AuditoriumType.STANDARD, 120);

        when(cinemaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> auditoriumService.updateAuditorium(99L, 1L, request))
                .isInstanceOf(CinemaNotFoundException.class)
                .hasMessage("Cinema not found with ID: 99");

        verify(auditoriumRepository, never()).findByCinemaIdAndId(anyLong(), anyLong());
    }

    @Test
    void updateAuditoriumReturnsNotFoundWhenAuditoriumDoesNotBelongToCinema() {

        AuditoriumReqDto request = new AuditoriumReqDto("Screen 1", AuditoriumType.STANDARD, 120);

        when(cinemaRepository.existsById(10L)).thenReturn(true);
        when(auditoriumRepository.findByCinemaIdAndId(10L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditoriumService.updateAuditorium(10L, 99L, request))
                .isInstanceOf(AuditoriumNotFoundException.class)
                .hasMessage("Auditorium not found with ID: 99");

        verify(auditoriumRepository).findByCinemaIdAndId(10L, 99L);
    }

    @Test
    void updateAuditoriumRejectsDuplicateNameWithinSameCinema() {

        Auditorium auditorium = auditorium("Screen 1", AuditoriumType.STANDARD, 120);
        AuditoriumReqDto request = new AuditoriumReqDto("IMAX Hall", AuditoriumType.IMAX, 250);

        when(cinemaRepository.existsById(10L)).thenReturn(true);
        when(auditoriumRepository.findByCinemaIdAndId(10L, 1L)).thenReturn(Optional.of(auditorium));
        when(auditoriumRepository.existsByCinemaIdAndNameIgnoreCaseAndIdNot(10L, "IMAX Hall", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> auditoriumService.updateAuditorium(10L, 1L, request))
                .isInstanceOf(AuditoriumAlreadyExistsException.class)
                .hasMessage("An auditorium with the same name already exists in this cinema.");

        verify(auditoriumMapper, never()).updateEntityFromDto(any(AuditoriumReqDto.class), any(Auditorium.class));
        verify(auditoriumRepository, never()).save(any(Auditorium.class));
    }

    @Test
    void deleteAuditoriumSoftDeletesExistingAuditorium() {

        Auditorium auditorium = auditorium("Screen 1", AuditoriumType.STANDARD, 120);

        when(cinemaRepository.existsById(10L)).thenReturn(true);
        when(auditoriumRepository.findByCinemaIdAndId(10L, 1L)).thenReturn(Optional.of(auditorium));
        when(auditoriumRepository.save(auditorium)).thenReturn(auditorium);

        auditoriumService.deleteAuditorium(10L, 1L);

        assertThat(auditorium.isDeleted()).isTrue();

        verify(auditoriumRepository).save(auditorium);
    }

    @Test
    void deleteAuditoriumReturnsNotFoundWhenAuditoriumDoesNotBelongToCinema() {
        
        when(cinemaRepository.existsById(10L)).thenReturn(true);
        when(auditoriumRepository.findByCinemaIdAndId(10L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditoriumService.deleteAuditorium(10L, 99L))
                .isInstanceOf(AuditoriumNotFoundException.class)
                .hasMessage("Auditorium not found with ID: 99");

        verify(auditoriumRepository, never()).save(any(Auditorium.class));
    }

    private Auditorium auditorium(String name, AuditoriumType type, Integer capacity) {

        Auditorium auditorium = new Auditorium();
        auditorium.setName(name);
        auditorium.setType(type);
        auditorium.setCapacity(capacity);
        return auditorium;
    }
}
