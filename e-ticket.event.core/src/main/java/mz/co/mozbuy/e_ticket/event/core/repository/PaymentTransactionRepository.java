package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.PaymentMethodEntity;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// PaymentTransactionRepository.java
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    Optional<PaymentTransactionEntity> findByTransactionId(String transactionId);

    List<PaymentTransactionEntity> findByStatus(String status);

    @Query("SELECT p FROM PaymentTransactionEntity p WHERE p.status = :status AND p.createdAt < :date")
    List<PaymentTransactionEntity> findExpiredTransactions(@Param("status") String status, @Param("date") LocalDateTime date);

    Optional<PaymentTransactionEntity> findByProviderCheckoutId(String providerCheckoutId);

    // 🔥 MÉTODO PARA BUSCAR COM RELACIONAMENTOS
    @Query("SELECT pt FROM PaymentTransactionEntity pt " +
            "LEFT JOIN FETCH pt.event " +
            "LEFT JOIN FETCH pt.ticket " +
            "LEFT JOIN FETCH pt.sale " +
            "WHERE pt.transactionId = :transactionId")
    Optional<PaymentTransactionEntity> findByTransactionIdWithRelations(@Param("transactionId") String transactionId);

}