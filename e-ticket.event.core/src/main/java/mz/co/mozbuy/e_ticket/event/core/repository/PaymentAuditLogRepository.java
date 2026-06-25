//package mz.co.mozbuy.e_ticket.event.core.repository;
//
//
//
//import mz.co.mozbuy.e_ticket.event.core.model.PaymentAuditLogEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLogEntity, Long> {
//
//    List<PaymentAuditLogEntity> findByPaymentTransactionIdOrderByPerformedAtDesc(String paymentTransactionId);
//
//    List<PaymentAuditLogEntity> findByAction(String action);
//
//    List<PaymentAuditLogEntity> findByPerformedAtBetween(LocalDateTime start, LocalDateTime end);
//
//    @Query("SELECT pal FROM PaymentAuditLogEntity pal WHERE pal.paymentTransactionId = :transactionId AND pal.action = :action")
//    List<PaymentAuditLogEntity> findByTransactionIdAndAction(@Param("transactionId") String transactionId, @Param("action") String action);
//
//    @Query(value = "SELECT * FROM payment_audit_log WHERE payment_transaction_id = :transactionId ORDER BY performed_at DESC LIMIT :limit", nativeQuery = true)
//    List<PaymentAuditLogEntity> findLastLogs(@Param("transactionId") String transactionId, @Param("limit") int limit);
//}