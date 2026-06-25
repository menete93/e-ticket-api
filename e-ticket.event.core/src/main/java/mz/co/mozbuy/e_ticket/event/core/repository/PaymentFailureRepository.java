//package mz.co.mozbuy.e_ticket.event.core.repository;
//
//import mz.co.mozbuy.e_ticket.event.core.model.PaymentFailureEntity;
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
//public interface PaymentFailureRepository extends JpaRepository<PaymentFailureEntity, Long> {
//
//    List<PaymentFailureEntity> findByFailureCategory(String failureCategory);
//
//    List<PaymentFailureEntity> findByPaymentTransactionId(String paymentTransactionId);
//
//    Optional<PaymentFailureEntity> findFirstByPaymentTransactionIdOrderByAttemptNumberDesc(String paymentTransactionId);
//
//    List<PaymentFailureEntity> findByCanRetryTrueAndResolvedFalse();
//
//    // Usando createdAt do AuditableEntity
//    @Query("SELECT pf FROM PaymentFailureEntity pf WHERE pf.createdAt BETWEEN :start AND :end")
//    List<PaymentFailureEntity> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
//
//    // ✅ CORRIGIDO - removido resolvedAt (campo não existe)
//    @Modifying
//    @Query("UPDATE PaymentFailureEntity pf SET pf.resolved = true, pf.resolutionAction = :action, pf.resolvedBy = :resolvedBy WHERE pf.id = :id")
//    int markAsResolved(@Param("id") Long id, @Param("action") String action, @Param("resolvedBy") String resolvedBy);
//
//    // Usando createdAt do AuditableEntity
//    long countByFailureCategoryAndCreatedAtBetween(String category, LocalDateTime start, LocalDateTime end);
//}