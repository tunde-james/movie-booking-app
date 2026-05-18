package com.example.moviebookingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moviebookingapp.entity.Movie;

public interface MovieRespository extends JpaRepository<Movie, Long> {

    boolean existsByTitle(String title);
}
