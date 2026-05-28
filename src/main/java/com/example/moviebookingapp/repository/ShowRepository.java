package com.example.moviebookingapp.repository;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.enums.ShowStatus;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("""
            select case when count(scheduledShow) > 0 then true else false end
            from Show scheduledShow
            where scheduledShow.auditorium.id = :auditoriumId
              and scheduledShow.status = :status
              and scheduledShow.deleted = false
              and scheduledShow.startTime < :bufferedEnd
              and scheduledShow.endTime > :bufferedStart
            """)
    boolean existsOverlappingScheduledShow(
            @Param("auditoriumId") Long auditoriumId,
            @Param("status") ShowStatus status,
            @Param("bufferedStart") OffsetDateTime bufferedStart,
            @Param("bufferedEnd") OffsetDateTime bufferedEnd);
}
