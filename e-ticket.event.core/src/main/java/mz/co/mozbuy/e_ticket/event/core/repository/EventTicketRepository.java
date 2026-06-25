package mz.co.mozbuy.e_ticket.event.core.repository;



import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import mz.co.mozbuy.e_ticket.event.core.model.EventTicket;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventTicketRepository extends JpaRepository<EventTicket, Long>, JpaSpecificationExecutor<EventTicket> {


    @Query("SELECT t FROM EventTicket t " +
            "WHERE t.event.id = :eventId " +
            "AND t.category = :category " +
            "AND t.state =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE " +
            "AND t.event.state =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    Optional<EventTicket> findActiveTicketsByEventIdAndCategory(@Param("eventId") Long eventId,
                                                                @Param("category") TicketCategory category);

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.state = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND t.availableQuantity > 0")
    List<EventTicket> findAvailableTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.state = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND t.hasDynamicPricing = true")
    List<EventTicket> findTicketsWithDynamicPricingByEventId(@Param("eventId") Long eventId);

    @Query("SELECT SUM(t.availableQuantity) FROM EventTicket t WHERE t.event.id = :eventId AND t.state = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    Integer sumAvailableTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT SUM(t.soldQuantity) FROM EventTicket t WHERE t.event.id = :eventId AND t.state = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    Integer sumSoldTicketsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT t FROM EventTicket t WHERE t.state = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND t.hasDynamicPricing = true")
    List<EventTicket> findAllTicketsWithDynamicPricing();

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.category = :category AND t.state = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND t.availableQuantity > 0")
    Optional<EventTicket> findAvailableTicketByEventAndCategory(@Param("eventId") Long eventId, @Param("category") TicketCategory category);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM EventTicket t " +
            "WHERE t.event.id = :eventId " +
            "AND t.category = :category " +
            "AND t.state = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE " +
            "AND t.event.state = mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE")
    boolean existsActiveTicketsByEventIdAndCategory(@Param("eventId") Long eventId,
                                                    @Param("category") TicketCategory category);



//    // NOVOS MÉTODOS NO EventTicketRepository
//    @Query("SELECT COUNT(et) FROM EventTicket et " +
//            "WHERE et.event.id = :eventId " +
//            "AND (:category IS NULL OR et.category = :category)")
//    long countByEventIdAndCategory(@Param("eventId") Long eventId,
//                                   @Param("category") TicketCategory category);







    // ==================== MÉTODOS COM LOCK ====================

    /**
     * Busca ticket com lock pessimista padrão (FOR UPDATE)
     * Aguarda se outro lock estiver ativo
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM EventTicket t WHERE t.id = :id")
    Optional<EventTicket> findByIdWithLock(@Param("id") Long id);

    /**
     * Busca ticket com SKIP LOCKED - NÃO espera por outros locks
     * Ideal para alta concorrência - apenas pula registros bloqueados
     * Necessário usar native query pois JPA não suporta SKIP LOCKED nativamente
     */
    @Query(value = "SELECT * FROM event_tickets WHERE id = :id FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<EventTicket> findByIdWithSkipLocked(@Param("id") Long id);

    /**
     * Busca ticket com NOWAIT - lança exceção se estiver bloqueado
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
    @Query("SELECT t FROM EventTicket t WHERE t.id = :id")
    Optional<EventTicket> findByIdWithNoWait(@Param("id") Long id);

    // ==================== MÉTODOS DE RESERVA E ESTOQUE ====================

    /**
     * Reserva tickets (atualiza reserved_quantity e available_quantity)
     */
    @Modifying
    @Query("UPDATE EventTicket t SET " +
            "t.reservedQuantity = t.reservedQuantity + :quantity, " +
            "t.availableQuantity = t.availableQuantity - :quantity, " +
            "t.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE t.id = :ticketId " +
            "AND (t.availableQuantity - t.reservedQuantity) >= :quantity")
    int reserveTickets(@Param("ticketId") Long ticketId, @Param("quantity") int quantity);

    /**
     * Confirma reserva (move reserved_quantity para sold_quantity)
     */
    @Modifying
    @Query("UPDATE EventTicket t SET " +
            "t.reservedQuantity = t.reservedQuantity - :quantity, " +
            "t.soldQuantity = t.soldQuantity + :quantity, " +
            "t.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE t.id = :ticketId " +
            "AND t.reservedQuantity >= :quantity")
    int confirmReservation(@Param("ticketId") Long ticketId, @Param("quantity") int quantity);

    /**
     * Libera reserva (devolve reserved_quantity para available_quantity)
     */
    @Modifying
    @Query("UPDATE EventTicket t SET " +
            "t.reservedQuantity = t.reservedQuantity - :quantity, " +
            "t.availableQuantity = t.availableQuantity + :quantity, " +
            "t.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE t.id = :ticketId " +
            "AND t.reservedQuantity >= :quantity")
    int releaseReservation(@Param("ticketId") Long ticketId, @Param("quantity") int quantity);

    // ==================== MÉTODOS DE CONSULTA ====================

    List<EventTicket> findByEventId(Long eventId);

    List<EventTicket> findByEventIdAndState(Long eventId, LifeCycleState state);

    @Query("SELECT t FROM EventTicket t WHERE t.event.id = :eventId AND t.state = 'ACTIVE'")
    List<EventTicket> findActiveByEventId(@Param("eventId") Long eventId);

    @Query("SELECT t FROM EventTicket t WHERE t.id = :ticketId AND t.state = 'ACTIVE'")
    Optional<EventTicket> findActiveById(@Param("ticketId") Long ticketId);

    /**
     * Verifica disponibilidade de estoque
     */
    @Query("SELECT (t.availableQuantity - t.reservedQuantity) FROM EventTicket t WHERE t.id = :ticketId")
    int getAvailableStock(@Param("ticketId") Long ticketId);

    /**
     * Busca tickets com estoque baixo
     */
    @Query("SELECT t FROM EventTicket t WHERE (t.availableQuantity - t.reservedQuantity) <= :threshold AND t.state = 'ACTIVE'")
    List<EventTicket> findLowStockTickets(@Param("threshold") int threshold);

    /**
     * Conta tickets por evento e categoria
     */
    long countByEventIdAndCategory(Long eventId, TicketCategory category);

    /**
     * Atualiza preço do ticket
     */
    @Modifying
    @Query("UPDATE EventTicket t SET t.currentPrice = :newPrice, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :ticketId")
    int updatePrice(@Param("ticketId") Long ticketId, @Param("newPrice") BigDecimal newPrice);


}