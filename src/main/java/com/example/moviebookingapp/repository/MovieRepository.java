package com.example.moviebookingapp.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieStatus;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    boolean existsByTitleIgnoreCaseAndReleaseDateAndLanguage(String title, LocalDate releasDate, Language language);

    List<Movie> findByMoviesStatusIn(Collection<MovieStatus> statuses);
}
