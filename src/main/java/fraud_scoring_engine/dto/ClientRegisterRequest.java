package fraud_scoring_engine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRegisterRequest {

    @NotBlank(message = "name is required")
    private String name;
}