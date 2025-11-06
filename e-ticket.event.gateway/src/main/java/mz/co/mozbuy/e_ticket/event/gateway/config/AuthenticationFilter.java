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
            "/api/auth/",
            "/api/public/",
            "/actuator/",
            "/h2-console/",
            "/v3/api-docs",
            "/swagger-ui",
            "/favicon.ico"
    );

    private static final List<String> ADMIN_ENDPOINTS = List.of(
            "/api/admin/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // ✅ Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // ✅ Get Authorization header
        String authHeader = getAuthHeader(request);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // ✅ Validate JWT token
        if (!jwtTokenValidator.validateToken(token)) {
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        // ✅ Check token expiration
        if (jwtTokenValidator.isTokenExpired(token)) {
            return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
        }

        // ✅ Extract user information from token
        String username = jwtTokenValidator.extractUsername(token);
        if (username == null) {
            return onError(exchange, "Unable to extract user from token", HttpStatus.UNAUTHORIZED);
        }

        // ✅ Check admin routes
        if (isAdminEndpoint(path)) {
            if (!hasAdminRole(token)) {
                return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
            }
        }

        // ✅ Add user headers to the request
        ServerHttpRequest modifiedRequest = addUserHeaders(request, username, token);

        log.info("Authenticated request - User: {}, Path: {}", username, path);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
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

    private boolean hasAdminRole(String token) {
        // Implement role extraction from token
        // This depends on how you structure your JWT claims
        try {
            var claims = jwtTokenValidator.extractAllClaims(token);
            String role = claims.get("role", String.class);
            return "ADMIN".equals(role) || "ROLE_ADMIN".equals(role);
        } catch (Exception e) {
            log.warn("Failed to extract role from token: {}", e.getMessage());
            return false;
        }
    }

    private ServerHttpRequest addUserHeaders(ServerHttpRequest request, String username, String token) {
        return request.mutate()
                .header("X-User-Id", username)
                .header("X-User-Auth", "Bearer " + token)
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