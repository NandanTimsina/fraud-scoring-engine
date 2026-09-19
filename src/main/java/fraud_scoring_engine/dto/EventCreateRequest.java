package fraud_scoring_engine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class EventCreateRequest {

    @NotNull(message = "clientId is required")
    private UUID clientId;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "type is required")
    private String type;

    private Map<String, Object> metadata;

    private Instant eventTimestamp; // optional — defaults to now if not provided
}