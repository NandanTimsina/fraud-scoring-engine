package fraud_scoring_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private UUID clientId;
    private String userId;
    private String type;
    private Map<String, Object> metadata;
    private Instant eventTimestamp;
    private Instant createdAt;
}