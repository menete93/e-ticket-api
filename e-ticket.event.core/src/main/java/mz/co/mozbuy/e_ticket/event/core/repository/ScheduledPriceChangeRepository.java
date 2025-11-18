package mz.co.mozbuy.e_ticket.event.core.repository;


import mz.co.mozbuy.e_ticket.event.core.model.ScheduledPriceChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledPriceChangeRepository extends JpaRepository<ScheduledPriceChange, Long>, JpaSpecificationExecutor<ScheduledPriceChange> {

    List<ScheduledPriceChange> findByPricingStrategyId(Long pricingStrategyId);

    List<ScheduledPriceChange> findByEventTicketId(Long eventTicketId);

    List<ScheduledPriceChange> findByIsExecutedFalse();

    List<ScheduledPriceChange> findByScheduledAtBeforeAndIsExecutedFalse(LocalDateTime scheduledAt);

    List<ScheduledPriceChange> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT spc FROM ScheduledPriceChange spc WHERE spc.pricingStrategy.event.id = :eventId")
    List<ScheduledPriceChange> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT spc FROM ScheduledPriceChange spc WHERE spc.pricingStrategy.event.id = :eventId AND spc.isExecuted = false")
    List<ScheduledPriceChange> findPendingChangesByEventId(@Param("eventId") Long eventId);

    @Query("SELECT spc FROM ScheduledPriceChange spc WHERE spc.eventTicket.id = :ticketId AND spc.isExecuted = false ORDER BY spc.scheduledAt ASC")
    List<ScheduledPriceChange> findPendingChangesByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT COUNT(spc) FROM ScheduledPriceChange spc WHERE spc.pricingStrategy.id = :strategyId AND spc.isExecuted = false")
    Long countPendingChangesByStrategyId(@Param("strategyId") Long strategyId);

    @Query("SELECT spc FROM ScheduledPriceChange spc WHERE spc.scheduledAt <= :now AND spc.isExecuted = false ORDER BY spc.scheduledAt ASC")
    List<ScheduledPriceChange> findDueChanges(@Param("now") LocalDateTime now);

    boolean existsByPricingStrategyIdAndIsExecutedFalse(Long pricingStrategyId);
}