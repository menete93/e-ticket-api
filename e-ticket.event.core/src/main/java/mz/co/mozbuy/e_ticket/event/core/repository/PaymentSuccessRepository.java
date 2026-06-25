//package mz.co.mozbuy.e_ticket.event.core.repository;
//
//
//
//import mz.co.mozbuy.e_ticket.event.core.model.PaymentSuccessEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface PaymentSuccessRepository extends JpaRepository<PaymentSuccessEntity, Long> {
//
//
//    List<PaymentSuccessEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
//
//    Optional<PaymentSuccessEntity> findByPaymentTransactionId(String paymentTransactionId);
//    Optional<PaymentSuccessEntity> findByProviderTransactionId(String providerTransactionId);
//    boolean existsByPaymentTransactionId(String paymentTransactionId);
//}