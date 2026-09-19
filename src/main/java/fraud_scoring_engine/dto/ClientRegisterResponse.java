package fraud_scoring_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ClientRegisterResponse {

    private UUID clientId;
    private String name;
    private String apiKey;
}