package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

//    List<TicketEntity> findByUserId(Long userId);

    List<TicketEntity> findByEventId(Long eventId);

    List<TicketEntity> findByCategoryId(Long categoryId);

    TicketEntity findByQrCode(String qrCode);
}
