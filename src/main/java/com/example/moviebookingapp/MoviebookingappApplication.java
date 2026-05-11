package com.example.moviebookingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoviebookingappApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviebookingappApplication.class, args);
	}

}
