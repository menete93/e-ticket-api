package mz.co.mozbuy.e_ticket.event.core.repository;



import jakarta.persistence.LockModeType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventTicketRepository extends JpaRepository<EventTicket, Long>, JpaSpecificationExecutor<EventTicket> {

    List<EventTicket> findByEventId(Long eventId);


    @Query("SELECT t FROM EventTicket t " +
            "WHERE t.event.id = :eventId " +
            "AND t.category = :category " +
            "AND t.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE " +
            "AND t.event.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    Optional<EventTicket> findActiveTicketsByEventIdAndCategory(@Param("eventId") Long eventId,
                                                                @Param("category") TicketCategory category);

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND t.availableQuantity > 0")
    List<EventTicket> findAvailableTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND t.hasDynamicPricing = true")
    List<EventTicket> findTicketsWithDynamicPricingByEventId(@Param("eventId") Long eventId);

    @Query("SELECT SUM(t.availableQuantity) FROM EventTicket t WHERE t.event.id = :eventId AND t.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    Integer sumAvailableTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT SUM(t.soldQuantity) FROM EventTicket t WHERE t.event.id = :eventId AND t.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    Integer sumSoldTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT t FROM EventTicket t WHERE t.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND t.hasDynamicPricing = true")
    List<EventTicket> findAllTicketsWithDynamicPricing();

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.category = :category AND t.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND t.availableQuantity > 0")
    Optional<EventTicket> findAvailableTicketByEventAndCategory(@Param("eventId") Long eventId, @Param("category") TicketCategory category);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM EventTicket t " +
            "WHERE t.event.id = :eventId " +
            "AND t.category = :category " +
            "AND t.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE " +
            "AND t.event.lifeCycleState = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    boolean existsActiveTicketsByEventIdAndCategory(@Param("eventId") Long eventId,
                                                    @Param("category") TicketCategory category);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT t FROM EventTicket t WHERE t.id = :id")
        Optional<EventTicket> findByIdWithLock(@Param("id") Long id);

        // Adicionar outras queries otimizadas
        @Query("SELECT t FROM EventTicket t JOIN FETCH t.event e WHERE t.id = :id")
        Optional<EventTicket> findByIdWithEvent(@Param("id") Long id);


    // NOVOS MÉTODOS NO EventTicketRepository
    @Query("SELECT COUNT(et) FROM EventTicket et " +
            "WHERE et.event.id = :eventId " +
            "AND (:category IS NULL OR et.category = :category)")
    long countByEventIdAndCategory(@Param("eventId") Long eventId,
                                   @Param("category") TicketCategory category);

    List<EventTicket> findByEventIdAndCategory(Long eventId, TicketCategory category);

}