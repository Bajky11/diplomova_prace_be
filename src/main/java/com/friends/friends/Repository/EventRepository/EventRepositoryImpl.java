package com.friends.friends.Repository.EventRepository;

import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Repository.EventRepository.EventRepositoryCustom;
import com.vladmihalcea.hibernate.type.array.LongArrayType;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class EventRepositoryImpl implements EventRepositoryCustom {

    private final EntityManager entityManager;

    public EventRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Event> getFilteredEventsWithCursor(Long cursorEventId, LocalDateTime cursorTime, String title, Long[] categoryIds, LocalDateTime startTime, LocalDateTime endTime, Double latitude, Double longitude, Double radiusKm) {
        String sql = """
                    SELECT * FROM get_filtered_events_with_cursor(
                        :cursorEventId,
                        :cursorTime,
                        :title,
                        :categoryIds,
                        :startTime,
                        :endTime,
                        :latitude,
                        :longitude,
                        :radiusKm
                    )
                """;

        Session session = entityManager.unwrap(Session.class);
        NativeQuery<Event> query = session.createNativeQuery(sql, Event.class);

        query.setParameter("cursorEventId", cursorEventId);
        query.setParameter("cursorTime", cursorTime);
        query.setParameter("title", title);

        query.unwrap(NativeQuery.class).setParameter("categoryIds", categoryIds, LongArrayType.INSTANCE);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        query.setParameter("latitude", latitude);
        query.setParameter("longitude", longitude);
        query.setParameter("radiusKm", radiusKm);

        return query.getResultList();
    }
}