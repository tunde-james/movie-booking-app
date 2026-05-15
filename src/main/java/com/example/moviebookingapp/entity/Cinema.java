package com.example.moviebookingapp.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cinemas")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Cinema extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "screens_type")
    private String screenType;

    @Column(name = "total_screens", nullable = false)
    private Integer totalScreens;

    @OneToMany(mappedBy = "cinema", fetch = FetchType.LAZY)
    private List<Show> shows = new ArrayList<>();
}
