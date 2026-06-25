//package mz.co.mozbuy.e_ticket.event.core.repository;
//
//import mz.co.mozbuy.e_ticket.event.core.model.PaymentCallbackQueueEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface PaymentCallbackQueueRepository extends JpaRepository<PaymentCallbackQueueEntity, Long> {
//
//
//    List<PaymentCallbackQueueEntity> findByStatusAndRetryCountLessThan(String status, int maxRetries);
//
//    Optional<PaymentCallbackQueueEntity> findByPaymentTransactionIdAndCallbackType(String paymentTransactionId, String callbackType);
//
//    @Modifying
//    @Query("UPDATE PaymentCallbackQueueEntity pcq SET pcq.status = :status, pcq.processedAt = CURRENT_TIMESTAMP WHERE pcq.id = :id")
//    int markAsProcessed(@Param("id") Long id, @Param("status") String status);
//
//    @Modifying
//    @Query("UPDATE PaymentCallbackQueueEntity pcq SET pcq.retryCount = pcq.retryCount + 1, pcq.errorMessage = :error WHERE pcq.id = :id")
//    int incrementRetry(@Param("id") Long id, @Param("error") String error);
//
//    long countByStatus(String status);
//
//    List<PaymentCallbackQueueEntity> findByStatusOrderByReceivedAtAsc(String status);
//    Optional<PaymentCallbackQueueEntity> findByPaymentTransactionId(String paymentTransactionId);
//
//    @Modifying
//    @Query("UPDATE PaymentCallbackQueueEntity q SET q.status = 'PROCESSED', q.processedAt = CURRENT_TIMESTAMP WHERE q.id = :id")
//    int markAsProcessed(@Param("id") Long id);
//}
