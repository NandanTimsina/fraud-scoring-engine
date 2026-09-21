package fraud_scoring_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResponse {

    private UUID clientId;
    private String userId;
    private int score;
    private List<Map<String, Object>> triggeredRules;
    private int eventCount;
    private Instant computedAt;
}