package fraud_scoring_engine.service;

import fraud_scoring_engine.dto.ScoreResponse;
import fraud_scoring_engine.model.Client;
import fraud_scoring_engine.model.Event;
import fraud_scoring_engine.model.RiskAssessment;
import fraud_scoring_engine.repository.ClientRepository;
import fraud_scoring_engine.repository.EventRepository;
import fraud_scoring_engine.repository.RiskAssessmentRepository;
import fraud_scoring_engine.scoring.RulesEngine;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ScoreService {

    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RulesEngine rulesEngine = new RulesEngine();

    public ScoreService(EventRepository eventRepository,
                        ClientRepository clientRepository,
                        RiskAssessmentRepository riskAssessmentRepository) {
        this.eventRepository = eventRepository;
        this.clientRepository = clientRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
    }

    public ScoreResponse computeScore(UUID clientId, String userId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        List<Event> events = eventRepository.findByClientIdAndUserIdOrderByEventTimestampAsc(client.getId(), userId);

        RulesEngine.ScoreResult result = rulesEngine.evaluate(events);

        RiskAssessment assessment = new RiskAssessment();
        assessment.setClientId(client.getId());
        assessment.setUserId(userId);
        assessment.setScore(result.getScore());
        assessment.setTriggeredRules(result.getTriggeredRules());

        RiskAssessment saved = riskAssessmentRepository.save(assessment);

        return new ScoreResponse(
                saved.getClientId(),
                saved.getUserId(),
                saved.getScore(),
                saved.getTriggeredRules(),
                events.size(),
                saved.getComputedAt()
        );
    }
}