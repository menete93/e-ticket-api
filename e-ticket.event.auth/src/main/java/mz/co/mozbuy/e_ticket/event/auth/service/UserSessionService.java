package mz.co.mozbuy.e_ticket.event.auth.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.auth.model.UserSession;
import mz.co.mozbuy.e_ticket.event.auth.model.User;
import mz.co.mozbuy.e_ticket.event.auth.repository.UserSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    @Transactional
    public UserSession createSession(User user, String token, String ipAddress, String userAgent) {
        // Desativar sessões existentes do usuário (opcional - para single session)
        // deactivateAllUserSessions(user.getId());

        UserSession session = UserSession.builder()
                .user(user)
                .token(token)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusHours(24)) // 24 horas
                .active(true)
                .build();

        UserSession savedSession = userSessionRepository.save(session);
        log.info("Session created for user: {} from IP: {}", user.getUsername(), ipAddress);
        return savedSession;
    }

    @Transactional
    public void deactivateSession(String token) {
        userSessionRepository.findByToken(token).ifPresent(session -> {
            session.setActive(false);
            userSessionRepository.save(session);
            log.info("Session deactivated for user: {}", session.getUser().getUsername());
        });
    }

    @Transactional
    public void deactivateAllUserSessions(Long userId) {
        userSessionRepository.deactivateAllUserSessions(userId);
        log.info("All sessions deactivated for user ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public Optional<UserSession> findByToken(String token) {
        return userSessionRepository.findByToken(token);
    }

    @Transactional(readOnly = true)
    public List<UserSession> getActiveSessionsByUserId(Long userId) {
        return userSessionRepository.findActiveSessionsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isSessionValid(String token) {
        return userSessionRepository.findByToken(token)
                .map(session -> session.isActive() && session.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void refreshSession(String token) {
        userSessionRepository.findByToken(token).ifPresent(session -> {
            if (session.isActive()) {
                session.setExpiresAt(LocalDateTime.now().plusHours(24));
                userSessionRepository.save(session);
                log.debug("Session refreshed for user: {}", session.getUser().getUsername());
            }
        });
    }

    @Transactional
    public int cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = userSessionRepository.deleteExpiredSessions(now);
        log.info("Cleaned up {} expired sessions", deletedCount);
        return deletedCount;
    }

    @Transactional
    public SessionCleanupResult comprehensiveCleanup() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Desativar sessões expiradas que ainda estão ativas
        List<UserSession> expiredActiveSessions = userSessionRepository.findSessionsExpiringBefore(now);
        int deactivatedCount = 0;

        for (UserSession session : expiredActiveSessions) {
            if (session.isActive()) {
                session.setActive(false);
                userSessionRepository.save(session);
                deactivatedCount++;
                log.debug("Deactivated expired session for user: {}", session.getUser().getUsername());
            }
        }

        // 2. Deletar sessões inativas expiradas
        int deletedCount = userSessionRepository.deleteExpiredSessions(now);

        SessionCleanupResult result = new SessionCleanupResult(deactivatedCount, deletedCount);
        log.info("Session cleanup completed: {} deactivated, {} deleted", deactivatedCount, deletedCount);

        return result;
    }

    @Transactional(readOnly = true)
    public Long getActiveSessionsCount() {
        return userSessionRepository.countActiveSessions();
    }

    @Transactional(readOnly = true)
    public List<UserSession> getSessionsExpiringSoon() {
        return userSessionRepository.findSessionsExpiringBefore(LocalDateTime.now().plusHours(1));
    }

    // Agendamento para limpeza automática de sessões expiradas
    @Scheduled(cron = "0 0 2 * * ?") // Executa todos os dias às 2h da manhã
    @Transactional
    public void scheduledSessionCleanup() {
        log.info("Starting scheduled session cleanup...");
        SessionCleanupResult result = comprehensiveCleanup();
        log.info("Scheduled cleanup finished: {} sessions processed", result.getTotalProcessed());
    }

    // Classe para resultado da limpeza
    public static class SessionCleanupResult {
        private final int deactivatedCount;
        private final int deletedCount;

        public SessionCleanupResult(int deactivatedCount, int deletedCount) {
            this.deactivatedCount = deactivatedCount;
            this.deletedCount = deletedCount;
        }

        public int getDeactivatedCount() { return deactivatedCount; }
        public int getDeletedCount() { return deletedCount; }
        public int getTotalProcessed() { return deactivatedCount + deletedCount; }
    }
}
