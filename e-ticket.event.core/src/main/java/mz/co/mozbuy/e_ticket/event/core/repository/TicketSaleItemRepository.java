package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.TicketSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketSaleItemRepository extends JpaRepository<TicketSaleItem, Long> {

    List<TicketSaleItem> findBySaleId(Long saleId);
}