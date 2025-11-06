package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.SalePhaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalePhaseRepository extends JpaRepository<SalePhaseEntity, Long> {

    List<SalePhaseEntity> findByEventId(Long eventId);
}
