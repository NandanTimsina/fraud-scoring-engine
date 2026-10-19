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

import java.util.List;

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
        client.setName("Example Marketplace");
        client.setApiKeyHash("stored-hash");
        client.setActive(true);
        return client;
    }

    @Test
    void validKey_authenticatesClientAndContinues() throws Exception {
        Client client = activeClient();
        request.addHeader("X-API-Key", "good-key");
        when(clientRepository.findAll()).thenReturn(List.of(client));
        when(passwordEncoder.matches("good-key", "stored-hash")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(client);
        assertThat(chain.getRequest()).isNotNull();          // request was passed on
        verifyNoInteractions(entryPoint);
    }

    @Test
    void wrongKey_isRejectedAndRequestStops() throws Exception {
        request.addHeader("X-API-Key", "bad-key");
        when(clientRepository.findAll()).thenReturn(List.of(activeClient()));
        when(passwordEncoder.matches("bad-key", "stored-hash")).thenReturn(false);

        filter.doFilter(request, response, chain);

        verify(entryPoint).commence(eq(request), eq(response), any(AuthenticationException.class));
        assertThat(chain.getRequest()).isNull();             // chain was never called
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void missingKey_isNotAuthenticated() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(clientRepository);
        verify(entryPoint, never()).commence(any(), any(), any());
    }
}