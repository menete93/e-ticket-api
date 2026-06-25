//package mz.co.mozbuy.e_ticket.event.core.repository;
//
//import jakarta.persistence.LockModeType;
//import mz.co.mozbuy.common.audit.LifeCycleState;
//import mz.co.mozbuy.e_ticket.event.core.model.TicketReservationEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Lock;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface TicketReservationRepository extends JpaRepository<TicketReservationEntity, Long> {
//
//    // ==================== BUSCAS BÁSICAS ====================
//
//    Optional<TicketReservationEntity> findBySaleId(Long saleId);
//
//    Optional<TicketReservationEntity> findByPaymentTransactionId(String paymentTransactionId);
//
//    // ==================== BUSCAS POR ESTADO ====================
//
//    // ✅ CORRIGIDO - usando state em vez de status
//    @Query("SELECT r FROM TicketReservationEntity r WHERE r.userId = :userId AND r.eventId = :eventId AND r.state = :state")
//    Optional<TicketReservationEntity> findActiveByUserIdAndEventId(@Param("userId") Long userId,
//                                                                   @Param("eventId") Long eventId,
//                                                                   @Param("state") LifeCycleState state);
//
//    // ✅ CORRIGIDO - usando state
//    @Query("SELECT r FROM TicketReservationEntity r WHERE r.state = :state AND r.expiresAt <= :now")
//    List<TicketReservationEntity> findExpiredActiveReservations(@Param("state") LifeCycleState state,
//                                                                @Param("now") LocalDateTime now);
//
//    // ==================== ATUALIZAÇÕES ====================
//
//    // ✅ CORRIGIDO - usando state
//    @Modifying
//    @Query("UPDATE TicketReservationEntity r SET r.state = :state, r.cancelledAt = CURRENT_TIMESTAMP, r.cancelReason = 'Reserva expirada por timeout' WHERE r.state = :state AND r.expiresAt <= :now")
//    int expireOldReservations(@Param("state") LifeCycleState state,
//                              @Param("now") LocalDateTime now);
//
//    // ✅ CORRIGIDO - não precisa de parâmetro status (usa CONFIRMED diretamente)
//    @Modifying
//    @Query("UPDATE TicketReservationEntity r SET r.state = :state, r.confirmedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
//    int confirmReservation(@Param("id") Long id,
//                           @Param("state") LifeCycleState state);
//
//    // ==================== ESTATÍSTICAS ====================
//    long countByUserIdAndEventIdAndStateIn(Long userId, Long eventId, List<LifeCycleState> states);
//
//    List<TicketReservationEntity> findByUserIdAndEventIdOrderByCreatedAtDesc(Long userId, Long eventId);
//
//    // ==================== LOCK ====================
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT r FROM TicketReservationEntity r WHERE r.id = :id")
//    Optional<TicketReservationEntity> findByIdWithLock(@Param("id") Long id);
//}