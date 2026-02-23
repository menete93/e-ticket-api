package mz.co.mozbuy.e_ticket.event.auth.feignClient.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class AuthTokenService {

    @Value("${feign.auth.username:kiko_menete}")  // Use suas credenciais
    private String username;

    @Value("${feign.auth.password:password123}")
    private String password;

    @Value("${feign.auth.login-url:http://localhost:8085/api/auth/login}")
    private String loginUrl;

    private volatile String cachedToken;
    private volatile Instant expiry;

    private final WebClient webClient;

    public AuthTokenService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<String> getToken() {
        // Verificar cache (token válido por 24 horas - 86400000ms)
        if (cachedToken != null && expiry != null && Instant.now().isBefore(expiry)) {
            log.debug("Using cached auth token (expires: {})", expiry);
            return Mono.just(cachedToken);
        }

        log.info("Logging in to get auth token from: {}", loginUrl);

        LoginRequest loginRequest = new LoginRequest(username, password);

        return webClient.post()
                .uri(loginUrl)
                .bodyValue(loginRequest)
                .retrieve()
                .onStatus(status -> status.isError(), response -> {
                    log.error("Auth login failed: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Auth error response: {}", body);
                                return Mono.error(new RuntimeException(
                                        "Authentication failed: " + response.statusCode() + " - " + body));
                            });
                })
                .bodyToMono(LoginResponse.class)
                .doOnNext(response -> {
                    cachedToken = response.getToken();
                    // expiresIn está em milissegundos (86400000ms = 24 horas)
                    long expiresInSeconds = response.getExpiresIn() / 1000;
                    expiry = Instant.now().plusSeconds(expiresInSeconds - 300); // 5 minutos de margem

                    log.info("Auth successful! Token obtained, expires in {} hours",
                            expiresInSeconds / 3600);
                    log.debug("Token type: {}, User: {}",
                            response.getType(), response.getUser().getUsername());
                })
                .map(LoginResponse::getToken)
                .doOnError(error -> {
                    log.error("Failed to get auth token", error);
                    // Limpar cache em caso de erro
                    cachedToken = null;
                    expiry = null;
                });
    }

    @Data
    private static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @Data
    private static class LoginResponse {
        @JsonProperty("token")
        private String token;

        @JsonProperty("type")
        private String type;

        @JsonProperty("expiresIn")
        private long expiresIn;

        @JsonProperty("user")
        private UserInfo user;

        @Data
        public static class UserInfo {
            private Long id;
            private String username;
            private String email;
            private String firstName;
            private String lastName;
            private Boolean isOrganizer;
            private String lifecycleStatus;
        }
    }
}