package com.example.moviebookingapp.repository.specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.data.jpa.domain.Specification;

import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.enums.ShowStatus;

public class ShowSpecifications {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Lagos");

    private ShowSpecifications() {}

    public static Specification<Show> hasMovieId(Long movieId) {

        return (root, query, criteriaBuilder) -> movieId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("movie").get("id"), movieId);
    }

    public static Specification<Show> hasCinemaId(Long cinemaId) {

        return (root, query, criteriaBuilder) -> cinemaId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("auditorium").get("cinema").get("id"), cinemaId);
    }

    public static Specification<Show> hasAuditoriumId(Long auditoriumId) {

        return (root, query, criteriaBuilder) -> auditoriumId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("auditorium").get("id"), auditoriumId);
    }

    public static Specification<Show> movieTitleContains(String movieTitle) {

        return (root, query, criteriaBuilder) -> {
            if (movieTitle == null || movieTitle.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("movie").get("title")),
                    "%" + movieTitle.toLowerCase().trim() + "%");
        };
    }

    public static Specification<Show> cinemaNameContains(String cinemaName) {

        return (root, query, criteriaBuilder) -> {
            if (cinemaName == null || cinemaName.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("auditorium").get("cinema").get("name")),
                    "%" + cinemaName.toLowerCase().trim() + "%");
        };
    }

    public static Specification<Show> cinemaCityEquals(String city) {

        return (root, query, criteriaBuilder) -> {
            if (city == null || city.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("auditorium").get("cinema").get("city")),
                    city.toLowerCase().trim());
        };
    }

    public static Specification<Show> startsOnDate(LocalDate date) {

        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            OffsetDateTime startOfDay = date.atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();
            OffsetDateTime nextDay =
                    date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();

            return criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), startOfDay),
                    criteriaBuilder.lessThan(root.get("startTime"), nextDay));
        };
    }

    public static Specification<Show> hasStatus(ShowStatus status) {

        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }
}
