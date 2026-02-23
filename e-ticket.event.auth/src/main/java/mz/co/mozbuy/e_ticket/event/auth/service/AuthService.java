package mz.co.mozbuy.e_ticket.event.auth.service;



import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.auth.model.User;
import mz.co.mozbuy.e_ticket.event.auth.model.UserSession;
import mz.co.mozbuy.e_ticket.event.auth.repository.UserSessionRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserSessionRepository userSessionRepository;


    public User authenticate(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        return (User) authentication.getPrincipal();
    }

//    @Transactional
//    public User authenticate(String username, String password, String ipAddress, String userAgent) {
//        // 1️⃣ Autenticar usuário
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(username, password)
//        );
//
//        User user = (User) authentication.getPrincipal();
//
//        // 2️⃣ Desativar sessões antigas (garantir uma sessão ativa por vez)
//        userSessionRepository.deactivateAllUserSessions(user.getId());
//
//        // 3️⃣ Criar nova sessão
//        UserSession newSession = UserSession.builder()
//                .user(user)
//                .token(generateToken())
//                .ipAddress(ipAddress)
//                .userAgent(userAgent)
//                .expiresAt(LocalDateTime.now().plusDays(1)) // ajustar conforme sua regra
//                .active(true)
//                .build();
//
//        userSessionRepository.save(newSession);
//
//        return user;
//    }
//
//    private String generateToken() {
//        return UUID.randomUUID().toString();
//    }
}