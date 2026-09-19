package fraud_scoring_engine.dto;

import java.util.UUID;

public class ClientRegisterResponse {

    private UUID clientId;
    private String name;
    private String apiKey; // raw key — shown only this once

    public ClientRegisterResponse(UUID clientId, String name, String apiKey) {
        this.clientId = clientId;
        this.name = name;
        this.apiKey = apiKey;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public String getApiKey() {
        return apiKey;
    }
}