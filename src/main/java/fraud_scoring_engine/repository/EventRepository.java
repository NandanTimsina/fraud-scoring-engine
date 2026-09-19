package fraud_scoring_engine.repository;

import fraud_scoring_engine.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByClientIdAndUserIdOrderByEventTimestampAsc(UUID clientId, String userId);
}