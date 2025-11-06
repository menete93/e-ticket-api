package mz.co.mozbuy.e_ticket.event.auth.repository;





import mz.co.mozbuy.e_ticket.event.auth.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByToken(String token);

    @Query("SELECT us FROM UserSession us WHERE us.user.id = :userId AND us.active = true")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserSession us SET us.active = false WHERE us.user.id = :userId")
    void deactivateAllUserSessions(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.expiresAt < :now AND us.active = false")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(us) FROM UserSession us WHERE us.active = true")
    Long countActiveSessions();

    @Query("SELECT us FROM UserSession us WHERE us.expiresAt < :expiryTime")
    List<UserSession> findSessionsExpiringBefore(@Param("expiryTime") LocalDateTime expiryTime);

    @Query("SELECT us FROM UserSession us WHERE us.expiresAt < :now AND us.active = true")
    List<UserSession> findExpiredActiveSessions(@Param("now") LocalDateTime now);
}