package mz.co.mozbuy.e_ticket.event.auth.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.auth.model.User;
import mz.co.mozbuy.e_ticket.event.auth.dto.LoginRequest;
import mz.co.mozbuy.e_ticket.event.auth.dto.LoginResponse;
import mz.co.mozbuy.e_ticket.event.auth.dto.RegisterRequest;
import mz.co.mozbuy.e_ticket.event.auth.dto.UserContext;
import mz.co.mozbuy.e_ticket.event.auth.model.UserSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserSessionService userSessionService;

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // Autenticar usuário
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Carregar usuário
        User user = (User) authentication.getPrincipal();

        // Gerar token
        String jwtToken = jwtService.generateToken(user);

        // Atualizar último login
        userService.updateLastLogin(user.getUsername());

        // Criar sessão
        UserSession session = userSessionService.createSession(user, jwtToken, ipAddress, userAgent);

        // Criar response
        UserContext userContext = UserContext.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().getName())
                .build();

        return LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime() / 1000) // em segundos
                .user(userContext)
                .build();
    }

    @Transactional
    public User register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) // Será encryptado no service
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        return userService.registerUser(user);
    }

    @Transactional
    public void logout(String token) {
        userSessionService.deactivateSession(token);
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void logoutAllSessions(Long userId) {
        userSessionService.deactivateAllUserSessions(userId);
    }
}