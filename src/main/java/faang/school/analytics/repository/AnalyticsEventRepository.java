package faang.school.analytics.repository;

import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    Stream<AnalyticsEvent> findByReceiverIdAndEventType(long receiverId, EventType eventType);

    @Query(nativeQuery = true, value = """
            SELECT * FROM analytics_event
            WHERE receiver_id = :receiverId 
            AND event_type = :eventType
            AND received_at BETWEEN :from AND :to
            ORDER BY received_at DESC;
            """)
    List<AnalyticsEvent> findByReceiverIdAndEventTypeBetweenDates(long receiverId, String eventType, LocalDateTime from, LocalDateTime to);
}
