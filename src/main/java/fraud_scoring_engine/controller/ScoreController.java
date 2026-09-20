package fraud_scoring_engine.controller;

import fraud_scoring_engine.dto.ScoreResponse;
import fraud_scoring_engine.model.Client;
import fraud_scoring_engine.security.AuthenticatedClient;
import fraud_scoring_engine.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @GetMapping("/score/{userId}")
    public ResponseEntity<ScoreResponse> getScore(@PathVariable String userId) {
        Client client = AuthenticatedClient.get();
        ScoreResponse response = scoreService.computeScore(client.getId(), userId);
        return ResponseEntity.ok(response);
    }
}