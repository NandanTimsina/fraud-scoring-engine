package fraud_scoring_engine.service;

import fraud_scoring_engine.dto.EventResponse;
import fraud_scoring_engine.dto.UserEventsResponse;
import fraud_scoring_engine.model.Client;
import fraud_scoring_engine.model.Event;
import fraud_scoring_engine.repository.ClientRepository;
import fraud_scoring_engine.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;

    public ScoreService(EventRepository eventRepository, ClientRepository clientRepository) {
        this.eventRepository = eventRepository;
        this.clientRepository = clientRepository;
    }

    public UserEventsResponse getUserEvents(UUID clientId, String userId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        List<Event> events = eventRepository.findByClientIdAndUserIdOrderByEventTimestampAsc(client.getId(), userId);

        List<EventResponse> eventResponses = events.stream()
                .map(e -> new EventResponse(
                        e.getId(),
                        e.getClientId(),
                        e.getUserId(),
                        e.getType(),
                        e.getMetadata(),
                        e.getEventTimestamp(),
                        e.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new UserEventsResponse(client.getId(), userId, eventResponses.size(), eventResponses);
    }
}