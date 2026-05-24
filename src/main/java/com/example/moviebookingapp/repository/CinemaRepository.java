package com.example.moviebookingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moviebookingapp.entity.Cinema;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
