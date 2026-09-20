package fraud_scoring_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserEventsResponse {

    private UUID clientId;
    private String userId;
    private int eventCount;
    private List<EventResponse> events;
}