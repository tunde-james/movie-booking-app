package com.example.moviebookingapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class MoviebookingappApplicationTests {

	@Container
	@ServiceConnection
	static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

	@Test
	void contextLoads() {
	}

}
