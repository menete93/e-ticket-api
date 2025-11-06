package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

//    List<PaymentEntity> findByUserId(Long userId);

    List<PaymentEntity> findByStatus(String status);

    List<PaymentEntity> findByRelatedTypeAndRelatedId(String relatedType, Long relatedId);
}
