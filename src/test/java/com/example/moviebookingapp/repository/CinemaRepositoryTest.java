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
import com.example.moviebookingapp.entity.Cinema;

@SuppressWarnings("null")
@DataJpaTest
@Testcontainers
@Import(JpaAuditingConfig.class)
class CinemaRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private CinemaRepository cinemaRepository;

    @Test
    void duplicateNameCheckUsesCaseInsensitiveName() {

        cinemaRepository.save(cinema("Filmhouse Lekki", "Address 1", "Lagos"));

        boolean exists = cinemaRepository.existsByNameIgnoreCase("filmhouse lekki");

        assertThat(exists).isTrue();
    }

    @Test
    void databaseRejectsDuplicateNormalizedCinemaName() {

        cinemaRepository.saveAndFlush(cinema("Filmhouse Lekki", "Address 1", "Lagos"));

        assertThatThrownBy(() -> cinemaRepository.saveAndFlush(cinema(" filmhouse lekki ", "Address 2", "Abuja")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseAllowsDifferentCinemaNamesWithSameAddressAndCity() {

        cinemaRepository.save(cinema("Filmhouse Lekki", "Same Address", "Lagos"));
        cinemaRepository.save(cinema("Genesis Deluxe", "Same Address", "Lagos"));

        List<Cinema> cinemas = cinemaRepository.findAll();

        assertThat(cinemas).extracting(Cinema::getName).containsExactlyInAnyOrder("Filmhouse Lekki", "Genesis Deluxe");
    }

    private Cinema cinema(String name, String address, String city) {
        
        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        cinema.setCity(city);
        return cinema;
    }
}
