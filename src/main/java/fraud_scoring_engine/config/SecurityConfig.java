package fraud_scoring_engine.config;

import fraud_scoring_engine.security.ApiKeyAuthFilter;
import fraud_scoring_engine.security.ApiKeyAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiKeyAuthFilter apiKeyAuthFilter;
    private final ApiKeyAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(ApiKeyAuthFilter apiKeyAuthFilter,
                          ApiKeyAuthenticationEntryPoint authenticationEntryPoint) {
        this.apiKeyAuthFilter = apiKeyAuthFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(HttpMethod.POST, "/clients/register").permitAll()
                .anyRequest().authenticated());

        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(authenticationEntryPoint));

        http.addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}