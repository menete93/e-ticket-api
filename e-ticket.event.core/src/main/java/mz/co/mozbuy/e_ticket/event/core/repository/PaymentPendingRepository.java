//package mz.co.mozbuy.e_ticket.event.core.repository;
//
//
//
//import mz.co.mozbuy.e_ticket.event.core.model.PaymentPendingEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
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
//public interface PaymentPendingRepository extends JpaRepository<PaymentPendingEntity, Long> {
//
//
//    List<PaymentPendingEntity> findByPendingTypeAndStatus(String pendingType, String status);
//
//
//    @Modifying
//    @Query("UPDATE PaymentPendingEntity pp SET pp.status = :status WHERE pp.paymentTransactionId = :transactionId")
//    int updateStatus(@Param("transactionId") String transactionId, @Param("status") String status);
//
//    boolean existsByPaymentTransactionIdAndStatus(String paymentTransactionId, String status);
//
//
//    Optional<PaymentPendingEntity> findByPaymentTransactionId(String paymentTransactionId);
//    List<PaymentPendingEntity> findByStatusAndExpiresAtBefore(String status, LocalDateTime date);
//
//    @Modifying
//    @Query("UPDATE PaymentPendingEntity p SET p.status = 'EXPIRED' WHERE p.status = 'ACTIVE' AND p.expiresAt <= :now")
//    int expireOldPending(@Param("now") LocalDateTime now);
//}