package mz.co.mozbuy.e_ticket.event.core.repository;


import mz.co.mozbuy.e_ticket.event.core.model.UserEventReservationControlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEventReservationControlRepository extends JpaRepository<UserEventReservationControlEntity, Long> {
    List<UserEventReservationControlEntity> findByIsBlockedTrue();

    List<UserEventReservationControlEntity> findByActiveReservationsGreaterThan(Integer activeReservations);

    @Modifying
    @Query("UPDATE UserEventReservationControlEntity c SET c.isBlocked = true, c.blockReason = :reason, c.blockedAt = CURRENT_TIMESTAMP, c.state = 'BLOCKED' WHERE c.userId = :userId AND c.eventId = :eventId")
    int blockUser(@Param("userId") Long userId, @Param("eventId") Long eventId, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE UserEventReservationControlEntity c SET c.isBlocked = false, c.blockReason = null, c.blockedAt = null, c.state = 'ACTIVE' WHERE c.userId = :userId AND c.eventId = :eventId")
    int unblockUser(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT c FROM UserEventReservationControlEntity c WHERE c.userId = :userId")
    List<UserEventReservationControlEntity> findAllByUserId(@Param("userId") Long userId);


    Optional<UserEventReservationControlEntity> findByUserIdAndEventId(Long userId, Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM UserEventReservationControlEntity c WHERE c.userId = :userId AND c.eventId = :eventId")
    Optional<UserEventReservationControlEntity> findByUserIdAndEventIdWithLock(@Param("userId") Long userId, @Param("eventId") Long eventId);
}