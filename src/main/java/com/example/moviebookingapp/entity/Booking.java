package com.example.moviebookingapp.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.SQLRestriction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.moviebookingapp.enums.BookingStatus;

@Entity
@Table(name = "bookings")
@SQLRestriction("deleted =false")
@Getter
@Setter
@NoArgsConstructor
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(name = "number_of_seats", nullable = false)
    private Integer numberOfSeats;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.01", message = "Total price must be greater than 0")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    @NotNull(message = "Total price is required")
    private BookingStatus bookingStatus;

    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingTime;
}
