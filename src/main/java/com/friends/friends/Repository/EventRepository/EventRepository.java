package com.friends.friends.Repository.EventRepository;

import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Entity.Event.EventMapProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {


    List<Event> findByCreatedByIdOrderByStartTime(Long accountId);

    @Query("SELECT e FROM Event e WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(e.happeningOn.latitude)) * cos(radians(e.happeningOn.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(e.happeningOn.latitude)))) < :radius")
    List<Event> findByLocationWithinRadius(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("radius") double radius);


    @Query(
            value = """
                    SELECT * FROM events
                    WHERE start_time >= CURRENT_DATE
                    ORDER BY start_time ASC, id ASC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Event findFirstTodayEvent();

    @Query(
            value = """
                    SELECT * FROM events
                    WHERE start_time >= :date
                    ORDER BY start_time ASC, id ASC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Event findFirstEventByDate(@Param("date") LocalDateTime date);

    @Query(
            value = """
                    SELECT * FROM get_filtered_events_with_cursor(
                        :cursorEventId,
                        :cursorTime,
                        :limit,
                        :title,
                        :categoryIds,
                        :startTime,
                        :endTime,
                        :latitude,
                        :longitude,
                        :radiusKm
                    )
                    """,
            nativeQuery = true
    )
    List<Event> findFilteredEvents(
            @Param("cursorEventId") Long cursorEventId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("limit") Integer limit,
            @Param("title") String title,
            @Param("categoryIds") Long[] categoryIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    @Query(value = "SELECT * FROM get_events_in_bounds(:west, :south, :east, :north)", nativeQuery = true)
    List<EventMapProjection> getEventsInBounds(
            @Param("west") double west,
            @Param("south") double south,
            @Param("east") double east,
            @Param("north") double north
    );

    @Query(value = "SELECT * FROM get_events_in_radius(:latitude, :longitude, :radius_km, :limit)", nativeQuery = true)
    List<Event> getEventsInRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radius_km") int radius_km,
            @Param("limit") int limit

    );


}
