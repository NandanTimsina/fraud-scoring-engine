package fraud_scoring_engine.service;

import fraud_scoring_engine.dto.EventCreateRequest;
import fraud_scoring_engine.dto.EventResponse;
import fraud_scoring_engine.model.Event;
import fraud_scoring_engine.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventResponse createEvent(UUID clientId, EventCreateRequest request) {
        Event event = new Event();
        event.setClientId(clientId);
        event.setUserId(request.getUserId());
        event.setType(request.getType());
        event.setMetadata(request.getMetadata());
        event.setEventTimestamp(request.getEventTimestamp());

        Event saved = eventRepository.save(event);

        return new EventResponse(
                saved.getId(),
                saved.getClientId(),
                saved.getUserId(),
                saved.getType(),
                saved.getMetadata(),
                saved.getEventTimestamp(),
                saved.getCreatedAt()
        );
    }
}