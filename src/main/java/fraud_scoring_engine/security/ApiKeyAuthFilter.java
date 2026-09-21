package fraud_scoring_engine.security;

import fraud_scoring_engine.model.Client;
import fraud_scoring_engine.repository.ClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public ApiKeyAuthFilter(ClientRepository clientRepository,
                            PasswordEncoder passwordEncoder,
                            ApiKeyAuthenticationEntryPoint authenticationEntryPoint) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");
        System.out.println("DEBUG - Received API key header: [" + apiKey + "]");

        if (apiKey == null || apiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Client> matchedClient = findClientByRawKey(apiKey);

        if (matchedClient.isEmpty() || !matchedClient.get().isActive()) {
            authenticationEntryPoint.commence(request, response,
                    new AuthenticationServiceException("Invalid or inactive API key"));
            return;
        }

        Client client = matchedClient.get();

        var authentication = new UsernamePasswordAuthenticationToken(
                client, null, Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private Optional<Client> findClientByRawKey(String rawKey) {
        List<Client> allClients = clientRepository.findAll();
        return allClients.stream()
                .filter(c -> passwordEncoder.matches(rawKey, c.getApiKeyHash()))
                .findFirst();
    }
}