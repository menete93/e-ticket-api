package mz.co.mozbuy.e_ticket.event.core.repository;


import mz.co.mozbuy.e_ticket.event.core.model.TicketPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketPriceHistoryRepository extends JpaRepository<TicketPriceHistory, Long>, JpaSpecificationExecutor<TicketPriceHistory> {

    List<TicketPriceHistory> findByEventTicketId(Long eventTicketId);

    List<TicketPriceHistory> findByEventTicketIdOrderByChangedAtDesc(Long eventTicketId);

    List<TicketPriceHistory> findByEventTicketEventId(Long eventId);

    @Query("SELECT tph FROM TicketPriceHistory tph WHERE tph.eventTicket.id = :ticketId AND tph.changedAt BETWEEN :startDate AND :endDate ORDER BY tph.changedAt DESC")
    List<TicketPriceHistory> findByTicketIdAndDateRange(@Param("ticketId") Long ticketId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT tph FROM TicketPriceHistory tph WHERE tph.eventTicket.event.id = :eventId AND tph.changeType = :changeType ORDER BY tph.changedAt DESC")
    List<TicketPriceHistory> findByEventIdAndChangeType(@Param("eventId") Long eventId, @Param("changeType") String changeType);

    @Query("SELECT COUNT(tph) FROM TicketPriceHistory tph WHERE tph.eventTicket.id = :ticketId")
    Long countPriceChangesByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT tph FROM TicketPriceHistory tph WHERE tph.eventTicket.id = :ticketId AND tph.changedAt = (SELECT MAX(tph2.changedAt) FROM TicketPriceHistory tph2 WHERE tph2.eventTicket.id = :ticketId)")
    Optional<TicketPriceHistory> findLatestPriceChangeByTicketId(@Param("ticketId") Long ticketId);
}