package fraud_scoring_engine.controller;

import fraud_scoring_engine.dto.ClientRegisterRequest;
import fraud_scoring_engine.dto.ClientRegisterResponse;
import fraud_scoring_engine.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/register")
    public ResponseEntity<ClientRegisterResponse> register(@Valid @RequestBody ClientRegisterRequest request) {
        ClientRegisterResponse response = clientService.registerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}