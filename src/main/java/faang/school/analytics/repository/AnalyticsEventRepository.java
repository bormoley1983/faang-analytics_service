package faang.school.analytics.repository;

import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.stream.Stream;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    Stream<AnalyticsEvent> findByReceiverIdAndEventType(long receiverId, EventType eventType);

    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO analytics_event(event_id, receiver_id, actor_id, event_type, occurred_at, received_at)
            VALUES (:eventId, :receiverId, :actorId, :eventType, :occurredAt, :receivedAt)
            ON CONFLICT (event_id) DO NOTHING
            """)
    int insertIfAbsent(String eventId, long receiverId, long actorId, String eventType,
                       Instant occurredAt, Instant receivedAt);

    @Query(nativeQuery = true,
            value = """
            SELECT * FROM analytics_event
            WHERE receiver_id = :receiverId 
            AND event_type = :eventType
            AND occurred_at BETWEEN :from AND :to
            ORDER BY occurred_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM analytics_event
            WHERE receiver_id = :receiverId
            AND event_type = :eventType
            AND occurred_at BETWEEN :from AND :to
            """)
    Page<AnalyticsEvent> findByReceiverIdAndEventTypeBetweenDates(long receiverId, String eventType,
                                                                  Instant from, Instant to, Pageable pageable);
}
