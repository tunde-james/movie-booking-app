package com.example.moviebookingapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moviebookingapp.entity.Auditorium;

public interface AuditoriumRepository extends JpaRepository<Auditorium, Long> {

    boolean existsByCinemaIdAndNameIgnoreCase(Long cinemaId, String name);

    List<Auditorium> findByCinemaId(Long cinemaId);

    Optional<Auditorium> findByCinemaIdAndId(Long cinemaId, Long auditoriumId);

    boolean existsByCinemaIdAndNameIgnoreCaseAndIdNot(Long cinemaId, String name, Long auditoriumId);
}
