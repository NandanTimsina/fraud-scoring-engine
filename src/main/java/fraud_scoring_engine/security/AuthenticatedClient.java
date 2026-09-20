package fraud_scoring_engine.security;

import fraud_scoring_engine.model.Client;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticatedClient {

    public static Client get() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Client client)) {
            throw new IllegalStateException("No authenticated client found");
        }
        return client;
    }
}