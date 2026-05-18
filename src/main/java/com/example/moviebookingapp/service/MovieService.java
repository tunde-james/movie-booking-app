package com.example.moviebookingapp.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moviebookingapp.dtos.movie.MovieReqDto;
import com.example.moviebookingapp.dtos.movie.MovieResDto;
import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.exception.MovieAlreadyExistsException;
import com.example.moviebookingapp.mapper.MovieMapper;
import com.example.moviebookingapp.repository.MovieRespository;

@Service
public class MovieService {

    private final MovieRespository movieRespository;
    private final MovieMapper movieMapper;

    public MovieService(MovieRespository movieRespository, MovieMapper movieMapper) {
        this.movieRespository = movieRespository;
        this.movieMapper = movieMapper;
    }

    @Transactional
    public MovieResDto addMovie(MovieReqDto reqDto) {

        if (movieRespository.existsByTitle(reqDto.title())) {
            throw new MovieAlreadyExistsException(
                    "A movie with the same title, release date, and language already exists: " + reqDto.title());
        }

        Movie movie = movieMapper.toEntity(reqDto);

        try {
            Movie savedMovie = movieRespository.save(movie);
            return movieMapper.toDto(savedMovie);
        } catch (DataIntegrityViolationException ex) {
            throw new MovieAlreadyExistsException(
                    "A movie with the same title, release date, and language already exists.");
        }

    }

    @Transactional
    public MovieResDto updateMovie(Long id, MovieReqDto reqDto) {

        if (id == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
    }
}
