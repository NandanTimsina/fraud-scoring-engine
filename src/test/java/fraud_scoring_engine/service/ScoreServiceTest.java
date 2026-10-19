package fraud_scoring_engine.service;

import fraud_scoring_engine.dto.ScoreResponse;
import fraud_scoring_engine.model.Client;
import fraud_scoring_engine.model.Event;
import fraud_scoring_engine.model.RiskAssessment;
import fraud_scoring_engine.repository.ClientRepository;
import fraud_scoring_engine.repository.EventRepository;
import fraud_scoring_engine.repository.RiskAssessmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private RiskAssessmentRepository riskAssessmentRepository;

    private ScoreService service;

    @BeforeEach
    void setUp() {
        service = new ScoreService(eventRepository, clientRepository, riskAssessmentRepository);
    }

    @Test
    void computeScore_savesAssessmentAndReturnsScore() {
        UUID clientId = UUID.randomUUID();
        Client client = new Client();
        client.setId(clientId);

        Event event = new Event();
        event.setType("listing_posted");
        event.setEventTimestamp(Instant.now().minus(Duration.ofHours(1)));   // brand-new account -> 20 points

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(eventRepository.findByClientIdAndUserIdOrderByEventTimestampAsc(clientId, "user_1"))
                .thenReturn(List.of(event));
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(inv -> {
            RiskAssessment a = inv.getArgument(0);
            a.setComputedAt(Instant.now());
            return a;
        });

        ScoreResponse response = service.computeScore(clientId, "user_1");

        assertThat(response.getScore()).isEqualTo(20);
        assertThat(response.getEventCount()).isEqualTo(1);
        assertThat(response.getClientId()).isEqualTo(clientId);
        assertThat(response.getUserId()).isEqualTo("user_1");

        ArgumentCaptor<RiskAssessment> captor = ArgumentCaptor.forClass(RiskAssessment.class);
        verify(riskAssessmentRepository).save(captor.capture());
        assertThat(captor.getValue().getClientId()).isEqualTo(clientId);
        assertThat(captor.getValue().getUserId()).isEqualTo("user_1");
        assertThat(captor.getValue().getScore()).isEqualTo(20);
    }

    @Test
    void computeScore_unknownClient_returns404AndSavesNothing() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.computeScore(clientId, "user_1"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(404));

        verify(riskAssessmentRepository, never()).save(any());
    }
}