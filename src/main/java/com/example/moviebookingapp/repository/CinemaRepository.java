package com.example.moviebookingapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moviebookingapp.entity.Cinema;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    List<Cinema> findAllByDeletedFalse();

    Optional<Cinema> findByIdAndDeletedFalse(Long id);

    boolean existsByIdAndDeletedFalse(Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
