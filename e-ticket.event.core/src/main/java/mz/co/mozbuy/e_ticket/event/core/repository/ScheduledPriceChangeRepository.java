package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.e_ticket.event.core.model.ScheduledPriceChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledPriceChangeRepository extends JpaRepository<ScheduledPriceChange, Long> {

    // 🔥 CORREÇÃO: Método que você está tentando usar
    List<ScheduledPriceChange> findByScheduledAtLessThanEqualAndIsExecutedFalse(LocalDateTime now);

    // Buscar mudanças programadas por estratégia
    List<ScheduledPriceChange> findByPricingStrategyId(Long strategyId);

    // Buscar mudanças programadas por ticket
    List<ScheduledPriceChange> findByEventTicketId(Long ticketId);

    // Buscar mudanças programadas por evento
    @Query("SELECT spc FROM ScheduledPriceChange spc " +
            "WHERE spc.pricingStrategy.event.id = :eventId " +
            "ORDER BY spc.scheduledAt DESC")
    List<ScheduledPriceChange> findByEventId(@Param("eventId") Long eventId);

    // Buscar mudanças futuras (não executadas) para um evento
    @Query("SELECT spc FROM ScheduledPriceChange spc " +
            "WHERE spc.pricingStrategy.event.id = :eventId " +
            "AND spc.isExecuted = false " +
            "ORDER BY spc.scheduledAt ASC")
    List<ScheduledPriceChange> findPendingByEventId(@Param("eventId") Long eventId);

    // Buscar mudanças programadas em um período
    List<ScheduledPriceChange> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);

    // Buscar mudanças executadas
    List<ScheduledPriceChange> findByIsExecutedTrue();

    // Buscar mudanças não executadas
    List<ScheduledPriceChange> findByIsExecutedFalse();

    // Buscar mudanças não executadas ordenadas por data
    List<ScheduledPriceChange> findByIsExecutedFalseOrderByScheduledAtAsc();

    // Contar mudanças pendentes
    long countByIsExecutedFalse();

    // Contar mudanças pendentes por evento
    @Query("SELECT COUNT(spc) FROM ScheduledPriceChange spc " +
            "WHERE spc.pricingStrategy.event.id = :eventId " +
            "AND spc.isExecuted = false")
    long countPendingByEventId(@Param("eventId") Long eventId);

    // Buscar próximas mudanças (para notificações)
    @Query("SELECT spc FROM ScheduledPriceChange spc " +
            "WHERE spc.scheduledAt BETWEEN :now AND :nextHour " +
            "AND spc.isExecuted = false")
    List<ScheduledPriceChange> findUpcomingChanges(
            @Param("now") LocalDateTime now,
            @Param("nextHour") LocalDateTime nextHour);

    // Marcar múltiplas mudanças como executadas
    @Modifying
    @Transactional
    @Query("UPDATE ScheduledPriceChange spc SET spc.isExecuted = true, " +
            "spc.executedAt = :executedAt, spc.executionResult = :result " +
            "WHERE spc.id IN :ids")
    int markAsExecuted(
            @Param("ids") List<Long> ids,
            @Param("executedAt") LocalDateTime executedAt,
            @Param("result") String result);

    // Deletar mudanças antigas já executadas
    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduledPriceChange spc " +
            "WHERE spc.isExecuted = true AND spc.executedAt < :date")
    int deleteOldExecuted(@Param("date") LocalDateTime date);

    // Estatísticas
    @Query("SELECT COUNT(spc), spc.changeType, FUNCTION('DATE', spc.scheduledAt) " +
            "FROM ScheduledPriceChange spc " +
            "WHERE spc.pricingStrategy.event.id = :eventId " +
            "GROUP BY spc.changeType, FUNCTION('DATE', spc.scheduledAt)")
    List<Object[]> getStatisticsByEvent(@Param("eventId") Long eventId);


    List<ScheduledPriceChange> findByExecutedAtAfter(LocalDateTime date);

}