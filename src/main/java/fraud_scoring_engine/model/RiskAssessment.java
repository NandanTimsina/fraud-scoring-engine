package fraud_scoring_engine.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments")
@Data
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private Integer score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "triggered_rules", columnDefinition = "jsonb")
    private List<Map<String, Object>> triggeredRules;

    @Column(name = "computed_at", nullable = false, updatable = false)
    private Instant computedAt;

    @PrePersist
    protected void onCreate() {
        this.computedAt = Instant.now();
    }
}