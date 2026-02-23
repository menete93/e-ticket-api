package mz.co.mozbuy.e_ticket.event.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenValidator jwtTokenValidator;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/validate",
            "/api/public/",
            "/actuator/",
            "/h2-console/",
            "/v3/api-docs",
            "/swagger-ui",
            "/favicon.ico",
            "/api/auth/register"

    );

    private static final List<String> ADMIN_ENDPOINTS = List.of(
            "/event-categories/**"
//            "/api/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Get Authorization header
        String authHeader = getAuthHeader(request);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // Validate JWT token
        if (!jwtTokenValidator.validateToken(token) || jwtTokenValidator.isTokenExpired(token)) {
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        // Extract user information
        String username = jwtTokenValidator.extractUsername(token);
        if (username == null) {
            return onError(exchange, "Unable to extract user from token", HttpStatus.UNAUTHORIZED);
        }

        // Extract roles
        String rolesString = extractRoles(token);

        // Check admin routes
        if (isAdminEndpoint(path) && !rolesString.contains("ADMIN")) {
            return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
        }

        // Add headers
        ServerHttpRequest modifiedRequest = addUserHeaders(request, username, token, rolesString);

        log.info("Authenticated request - User: {}, Path: {}", username, path);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private String extractRoles(String token) {
        try {
            var claims = jwtTokenValidator.extractAllClaims(token);
            Object roles = claims.get("roles"); // depende de como os roles estão no JWT
            if (roles instanceof List<?>) {
                return String.join(",", ((List<?>) roles).stream()
                        .map(Object::toString)
                        .toList());
            } else if (roles != null) {
                return roles.toString();
            }
        } catch (Exception e) {
            log.warn("Failed to extract roles from token: {}", e.getMessage());
        }
        return "";
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminEndpoint(String path) {
        return ADMIN_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private String getAuthHeader(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        return (authHeaders != null && !authHeaders.isEmpty()) ? authHeaders.get(0) : null;
    }

    private ServerHttpRequest addUserHeaders(ServerHttpRequest request, String username, String token, String rolesString) {
        return request.mutate()
                .header("X-User-Id", username)
                .header("X-User-Auth", "Bearer " + token)
                .header("X-User-Roles", rolesString)
                .header("X-Authenticated", "true")
                .build();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        log.warn("Authentication failed: {} - Path: {}", error, exchange.getRequest().getPath());
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
