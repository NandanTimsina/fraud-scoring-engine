package fraud_scoring_engine.service;

import fraud_scoring_engine.dto.EventCreateRequest;
import fraud_scoring_engine.dto.EventResponse;
import fraud_scoring_engine.model.Client;
import fraud_scoring_engine.model.Event;
import fraud_scoring_engine.repository.ClientRepository;
import fraud_scoring_engine.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;

    public EventService(EventRepository eventRepository, ClientRepository clientRepository) {
        this.eventRepository = eventRepository;
        this.clientRepository = clientRepository;
    }

    public EventResponse createEvent(EventCreateRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        Event event = new Event();
        event.setClientId(client.getId());
        event.setUserId(request.getUserId());
        event.setType(request.getType());
        event.setMetadata(request.getMetadata());
        event.setEventTimestamp(request.getEventTimestamp()); // null is fine man

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