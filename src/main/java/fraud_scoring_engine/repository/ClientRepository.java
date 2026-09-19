package fraud_scoring_engine.repository;

import fraud_scoring_engine.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByApiKeyHash(String apiKeyHash);
}