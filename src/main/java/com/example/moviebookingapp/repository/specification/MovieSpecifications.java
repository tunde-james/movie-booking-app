package com.example.moviebookingapp.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieStatus;

public final class MovieSpecifications {

    private MovieSpecifications() {}

    public static Specification<Movie> titleContains(String title) {

        return (root, query, criteriaBuilder) -> {
            if (title == null || title.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Movie> hasGenre(Genre genre) {

        return (root, query, criteriaBuilder) ->
                genre == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("genre"), genre);
    }

    public static Specification<Movie> hasLanguage(Language language) {

        return (root, query, criteriaBuilder) -> language == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("language"), language);
    }

    public static Specification<Movie> hasStatus(MovieStatus status) {

        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("movieStatus"), status);
    }

    public static Specification<Movie> isPublicVisible() {

        return (root, query, criteriaBuilder) ->
                root.get("movieStatus").in(MovieStatus.COMING_SOON, MovieStatus.NOW_SHOWING);
    }
}
