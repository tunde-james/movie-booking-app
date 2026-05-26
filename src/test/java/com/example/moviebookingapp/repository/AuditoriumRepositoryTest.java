package com.example.moviebookingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.moviebookingapp.config.JpaAuditingConfig;
import com.example.moviebookingapp.entity.Auditorium;
import com.example.moviebookingapp.entity.Cinema;
import com.example.moviebookingapp.enums.AuditoriumType;

@SuppressWarnings("null")
@DataJpaTest
@Testcontainers
@Import(JpaAuditingConfig.class)
class AuditoriumRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Test
    void databaseRejectsDuplicateNormalizedAuditoriumNameWithinSameCinema() {

        Cinema cinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki"));

        auditoriumRepository.saveAndFlush(auditorium(cinema, "Screen 1"));

        assertThatThrownBy(() -> auditoriumRepository.saveAndFlush(auditorium(cinema, " screen 1 ")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseAllowsSameAuditoriumNameInDifferentCinemas() {

        Cinema firstCinema = cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki"));
        Cinema secondCinema = cinemaRepository.saveAndFlush(cinema("Genesis Deluxe"));

        auditoriumRepository.save(auditorium(firstCinema, "Screen 1"));
        auditoriumRepository.save(auditorium(secondCinema, "Screen 1"));

        List<Auditorium> auditoriums = auditoriumRepository.findAll();

        assertThat(auditoriums).hasSize(2);
    }

    private Cinema cinema(String name) {

        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress("Address");
        cinema.setCity("Lagos");
        return cinema;
    }

    private Auditorium auditorium(Cinema cinema, String name) {

        Auditorium auditorium = new Auditorium();
        auditorium.setCinema(cinema);
        auditorium.setName(name);
        auditorium.setType(AuditoriumType.STANDARD);
        auditorium.setCapacity(120);
        return auditorium;
    }
}
