package com.example.moviebookingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moviebookingapp.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {}
