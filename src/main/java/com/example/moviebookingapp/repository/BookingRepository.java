package com.example.moviebookingapp.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moviebookingapp.entity.Booking;
import com.example.moviebookingapp.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByShowIdAndBookingStatusInAndDeletedFalse(
            Long showId, Collection<BookingStatus> bookingStatuses);
}
