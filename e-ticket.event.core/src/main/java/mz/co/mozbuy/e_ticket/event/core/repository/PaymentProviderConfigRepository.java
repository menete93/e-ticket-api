package mz.co.mozbuy.e_ticket.event.core.repository;


import mz.co.mozbuy.e_ticket.event.core.model.PaymentProviderConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentProviderConfigRepository extends JpaRepository<PaymentProviderConfigEntity, Long> {

    Optional<PaymentProviderConfigEntity> findByCode(String code);
}