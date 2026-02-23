package mz.co.mozbuy.e_ticket.event.auth.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.auth.model.Role;
import mz.co.mozbuy.e_ticket.event.auth.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 horas padrão
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        // ✅ Extrai os nomes das roles
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // ✅ Extrai todas as authorities (roles + permissions)
        List<String> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add("ROLE_" + role.getName()); // Adiciona role com prefixo
            role.getPermissions().forEach(permission -> {
                authorities.add(permission.getName()); // Adiciona permissões
            });
        });

        claims.put("roles", roleNames);
        claims.put("authorities", authorities); // 🆕 Adiciona authorities no token
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("lifecyclestate", user.getLifeCycleState());
        claims.put("isOrganizer", user.getIsOrganizer());

        // Se for organizador, podemos adicionar um objeto organizer com mais dados, se disponíveis.
        if (Boolean.TRUE.equals(user.getIsOrganizer()) && user.getOrganizerReferenceId() != null) {
            // Criar um mapa com os dados do organizador que já estão no Auth Service
            Map<String, Object> organizerClaims = new HashMap<>();
            organizerClaims.put("referenceId", user.getOrganizerReferenceId());
            // Se você tiver mais campos no User relacionados ao organizador, adicione aqui.
            // Exemplo: organizerClaims.put("companyName", user.getCompanyName());

            claims.put("organizer", organizerClaims);
        }

        return buildToken(claims, user.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String getRoleFromToken(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Long getExpirationTime() {
        return jwtExpiration;
    }
}