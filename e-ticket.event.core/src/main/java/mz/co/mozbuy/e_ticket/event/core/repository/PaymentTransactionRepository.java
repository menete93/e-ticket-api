package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    Optional<PaymentTransactionEntity> findByReservationCode(String reservationCode);

    List<PaymentTransactionEntity> findByStatus(String status);

    List<PaymentTransactionEntity> findBySaleId(Long saleId);

    List<PaymentTransactionEntity> findByUserId(Long userId);

    List<PaymentTransactionEntity> findByStatusAndExpiresAtBefore(String status, LocalDateTime date);

    @Modifying
    @Query("UPDATE PaymentTransactionEntity pt SET pt.status = :status WHERE pt.reservationCode = :reservationCode")
    int updateStatus(@Param("reservationCode") String reservationCode, @Param("status") String status);

    @Modifying
    @Query("UPDATE PaymentTransactionEntity pt SET pt.status = 'EXPIRED' WHERE pt.status = 'PENDING' AND pt.expiresAt < :now")
    int expireAllPending(@Param("now") LocalDateTime now);
}