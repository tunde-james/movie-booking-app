package com.example.moviebookingapp.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.SQLRestriction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;

@Entity
@Table(
        name = "movies",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_movies_title_release_date_language_deleted",
                        columnNames = {"title", "release_date", "language", "deleted"}))
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Movie extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Column(nullable = false)
    private Integer durationInMinutes;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieRating rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus movieStatus;

    @Column(length = 500)
    private String posterUrl;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<Show> shows = new ArrayList<>();
}
