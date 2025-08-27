package com.friends.friends.Repository.EventRepository;

import com.friends.friends.Entity.Event.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> getFilteredEventsWithCursor(
            Long cursorEventId,
            LocalDateTime cursorTime,
            String title,
            Long[] categoryIds,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Double latitude,
            Double longitude,
            Double radiusKm
    );
}
