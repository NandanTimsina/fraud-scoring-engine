package fraud_scoring_engine.controller;

import fraud_scoring_engine.dto.UserEventsResponse;
import fraud_scoring_engine.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @GetMapping("/score/{userId}")
    public ResponseEntity<UserEventsResponse> getScore(
            @PathVariable String userId,
            @RequestParam UUID clientId) {

        UserEventsResponse response = scoreService.getUserEvents(clientId, userId);
        return ResponseEntity.ok(response);
    }
}