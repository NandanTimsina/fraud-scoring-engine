package fraud_scoring_engine.service;

import fraud_scoring_engine.dto.ClientRegisterRequest;
import fraud_scoring_engine.dto.ClientRegisterResponse;
import fraud_scoring_engine.model.Client;
import fraud_scoring_engine.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ClientRegisterResponse registerClient(ClientRegisterRequest request) {
        String rawApiKey = generateApiKey();
        String hashedApiKey = passwordEncoder.encode(rawApiKey);   // only the random part is hashed

        Client client = new Client();
        client.setName(request.getName());
        client.setApiKeyHash(hashedApiKey);

        Client saved = clientRepository.save(client);

        // The key given to the caller is "<clientId>.<randomPart>" so the filter
        // can find this one client directly instead of scanning all of them.

        String fullApiKey = saved.getId() + "." + rawApiKey;

        return new ClientRegisterResponse(saved.getId(), saved.getName(), fullApiKey);
    }

    private String generateApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}