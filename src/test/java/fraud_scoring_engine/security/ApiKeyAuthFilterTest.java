package fraud_scoring_engine.security;

import fraud_scoring_engine.model.Client;
import fraud_scoring_engine.repository.ClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthFilterTest {

    @Mock private ClientRepository clientRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ApiKeyAuthenticationEntryPoint entryPoint;

    private ApiKeyAuthFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    private final UUID clientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        filter = new ApiKeyAuthFilter(clientRepository, passwordEncoder, entryPoint);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Client activeClient() {
        Client client = new Client();
        client.setId(clientId);
        client.setName("Example Marketplace");
        client.setApiKeyHash("stored-hash");
        client.setActive(true);
        return client;
    }

    @Test
    void validKey_authenticatesClientAndContinues() throws Exception {
        Client client = activeClient();
        request.addHeader("X-API-Key", clientId + ".good-secret");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("good-secret", "stored-hash")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(client);
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(entryPoint);
    }

    @Test
    void wrongSecret_isRejectedAndRequestStops() throws Exception {
        request.addHeader("X-API-Key", clientId + ".bad-secret");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(activeClient()));
        when(passwordEncoder.matches("bad-secret", "stored-hash")).thenReturn(false);

        filter.doFilter(request, response, chain);

        verify(entryPoint).commence(eq(request), eq(response), any(AuthenticationException.class));
        assertThat(chain.getRequest()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void malformedKey_isRejectedWithoutTouchingTheDatabase() throws Exception {
        request.addHeader("X-API-Key", "no-dot-in-this-key");

        filter.doFilter(request, response, chain);

        verify(entryPoint).commence(eq(request), eq(response), any(AuthenticationException.class));
        verifyNoInteractions(clientRepository);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void missingKey_isNotAuthenticated() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(clientRepository);
        verify(entryPoint, never()).commence(any(), any(), any());
    }
}