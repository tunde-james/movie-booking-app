package com.example.moviebookingapp.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cinemas")
@Getter
@Setter
@NoArgsConstructor
public class Cinema extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "screens_type")
    private Integer screenType;

    @Column(name = "total_screens")
    private Integer totalScreens;

    @OneToMany(mappedBy = "cinema", fetch = FetchType.LAZY)
    private List<Show> shows = new ArrayList<>();
}
