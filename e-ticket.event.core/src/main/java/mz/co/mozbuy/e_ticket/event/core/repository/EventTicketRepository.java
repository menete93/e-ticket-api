package mz.co.mozbuy.e_ticket.event.core.repository;



import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventTicketRepository extends JpaRepository<EventTicket, Long>, JpaSpecificationExecutor<EventTicket> {

    List<EventTicket> findByEventId(Long eventId);

    List<EventTicket> findByEventIdAndIsActiveTrue(Long eventId);

    List<EventTicket> findByEventIdAndCategory(Long eventId, TicketCategory category);

    Optional<EventTicket> findByEventIdAndCategoryAndIsActiveTrue(Long eventId, TicketCategory category);

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.isActive = true AND t.availableQuantity > 0")
    List<EventTicket> findAvailableTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.isActive = true AND t.hasDynamicPricing = true")
    List<EventTicket> findTicketsWithDynamicPricingByEventId(@Param("eventId") Long eventId);

    @Query("SELECT SUM(t.availableQuantity) FROM EventTicket t WHERE t.event.id = :eventId AND t.isActive = true")
    Integer sumAvailableTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT SUM(t.soldQuantity) FROM EventTicket t WHERE t.event.id = :eventId AND t.isActive = true")
    Integer sumSoldTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT t FROM EventTicket t WHERE t.isActive = true AND t.hasDynamicPricing = true")
    List<EventTicket> findAllTicketsWithDynamicPricing();

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.category = :category AND t.isActive = true AND t.availableQuantity > 0")
    Optional<EventTicket> findAvailableTicketByEventAndCategory(@Param("eventId") Long eventId, @Param("category") TicketCategory category);

    boolean existsByEventIdAndCategoryAndIsActiveTrue(Long eventId, TicketCategory category);
}