package com.example.moviebookingapp.dtos.movie;

import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieStatus;

public record MovieSearchCriteria(String title, Genre genre, Language language, MovieStatus status) {}
