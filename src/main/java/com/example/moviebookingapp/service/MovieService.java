package com.example.moviebookingapp.service;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moviebookingapp.dtos.movie.MovieReqDto;
import com.example.moviebookingapp.dtos.movie.MovieResDto;
import com.example.moviebookingapp.dtos.movie.MovieSearchCriteria;
import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.exception.MovieAlreadyExistsException;
import com.example.moviebookingapp.exception.MovieNotFoundException;
import com.example.moviebookingapp.mapper.MovieMapper;
import com.example.moviebookingapp.repository.MovieRepository;
import com.example.moviebookingapp.repository.specification.MovieSpecifications;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public MovieService(MovieRepository movieRepository, MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    @Transactional(readOnly = true)
    public List<MovieResDto> getPublicMovies() {

        return searchMovies(new MovieSearchCriteria(null, null, null, null));
    }

    @Transactional(readOnly = true)
    public List<MovieResDto> searchMovies(MovieSearchCriteria criteria) {

        Specification<Movie> specification = MovieSpecifications.isPublicVisible();

        if (criteria != null) {
            specification = specification
                    .and(MovieSpecifications.titleContains(criteria.title()))
                    .and(MovieSpecifications.hasGenre(criteria.genre()))
                    .and(MovieSpecifications.hasLanguage(criteria.language()))
                    .and(MovieSpecifications.hasStatus(criteria.status()));
        }

        List<Movie> movies = movieRepository.findAll(specification);

        return movieMapper.toDtoList(movies);
    }

    @Transactional(readOnly = true)
    public MovieResDto getMovieById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Movie ID cannot be null");
        }

        Movie movie = movieRepository
                .findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + id));

        return movieMapper.toDto(movie);
    }

    @Transactional
    public MovieResDto addMovie(MovieReqDto reqDto) {

        MovieReqDto normalizedReqDto = normalizeMovieRequest(reqDto);

        if (movieRepository.existsByTitleIgnoreCaseAndReleaseDateAndLanguage(
                normalizedReqDto.title(), normalizedReqDto.releaseDate(), normalizedReqDto.language())) {
            throw new MovieAlreadyExistsException(
                    "A movie with the same title, release date, and language already exists.");
        }

        Movie movie =
                Objects.requireNonNull(movieMapper.toEntity(normalizedReqDto), "Movie mapper must not return null");

        try {
            Movie savedMovie = movieRepository.save(movie);
            return movieMapper.toDto(savedMovie);
        } catch (DataIntegrityViolationException ex) {
            throw new MovieAlreadyExistsException(
                    "A movie with the same title, release date, and language already exists.");
        }
    }

    @Transactional
    public MovieResDto updateMovie(Long id, MovieReqDto reqDto) {

        if (id == null) {
            throw new IllegalArgumentException("Movie ID cannot be null");
        }

        MovieReqDto normalizedReqDto = normalizeMovieRequest(reqDto);

        Movie movie = movieRepository
                .findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + id));

        if (movieRepository.existsByTitleIgnoreCaseAndReleaseDateAndLanguageAndIdNot(
                normalizedReqDto.title(), normalizedReqDto.releaseDate(), normalizedReqDto.language(), id)) {
            throw new MovieAlreadyExistsException(
                    "A movie with the same title, release date, and language already exists.");
        }

        movieMapper.updateEntityFromDto(normalizedReqDto, movie);

        try {
            Movie updatedMovie = movieRepository.save(movie);
            return movieMapper.toDto(updatedMovie);
        } catch (DataIntegrityViolationException ex) {
            throw new MovieAlreadyExistsException(
                    "A movie with the same title, release date, and language already exists.");
        }
    }

    @Transactional
    public void deleteMovie(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Movie with ID cannot be null");
        }

        Movie movie = movieRepository
                .findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + id));

        movie.setDeleted(true);

        movieRepository.save(movie);
    }

    private MovieReqDto normalizeMovieRequest(MovieReqDto reqDto) {

        return new MovieReqDto(
                reqDto.title().trim(),
                reqDto.description(),
                reqDto.genre(),
                reqDto.durationInMinutes(),
                reqDto.releaseDate(),
                reqDto.language(),
                reqDto.rating(),
                reqDto.movieStatus(),
                reqDto.posterUrl());
    }
}
