package fraud_scoring_engine.repository;

import fraud_scoring_engine.model.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    Optional<RiskAssessment> findTopByClientIdAndUserIdOrderByComputedAtDesc(UUID clientId, String userId);
}