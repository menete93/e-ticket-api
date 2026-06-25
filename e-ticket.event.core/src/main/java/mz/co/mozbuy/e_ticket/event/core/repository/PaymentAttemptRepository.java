package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.PaymentAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttemptEntity, Long> {

    List<PaymentAttemptEntity> findByPaymentTransactionId(Long paymentTransactionId);

    Optional<PaymentAttemptEntity> findFirstByPaymentTransactionIdOrderByAttemptNumberDesc(Long paymentTransactionId);

    long countByPaymentTransactionId(Long paymentTransactionId);
}