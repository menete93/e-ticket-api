package mz.co.mozbuy.e_ticket.event.core.repository;

import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PricingStrategyRepository extends JpaRepository<PricingStrategy, Long> {

    // ==================== MÉTODOS BÁSICOS ====================

    // Buscar estratégias ativas de um evento
    List<PricingStrategy> findByEventIdAndLifeCycleStateAfterOrderByPriorityDesc(Long eventId,LifeCycleState lifeCycleState);

    // Buscar estratégias ativas com auto-apply
    List<PricingStrategy> findByEventIdAndLifeCycleStateTrueAndAutoApplyTrueOrderByPriorityDesc(Long eventId);

    // Buscar todas estratégias ativas com auto-apply (para scheduler)
    @Query("SELECT ps FROM PricingStrategy ps WHERE ps.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE AND ps.autoApply = true")
    List<PricingStrategy> findActiveAndAutoApplied();

    // Buscar por tipo
    List<PricingStrategy> findByEventIdAndStrategyType(Long eventId, PricingStrategyType type);

    // Verificar se existe estratégia com mesmo nome no evento
    boolean existsByEventIdAndStrategyNameIgnoreCase(Long eventId, String StrategyName);

    // ==================== MÉTODOS DE PRIORIDADE ====================

    // 🔥 MÉTODO QUE VOCÊ PRECISA: Buscar estratégia de maior prioridade
//    Optional<PricingStrategy> findTopByEventIdAndLifeCycleStateTrueOrderByPriorityDesc(Long eventId);

    Optional<PricingStrategy> findTopByEventIdAndLifeCycleStateOrderByPriorityDesc(Long eventId, LifeCycleState lifecycle);

    // Versão alternativa com Query
    @Query("SELECT ps FROM PricingStrategy ps WHERE ps.event.id = :eventId AND ps.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE ORDER BY ps.priority DESC")
    List<PricingStrategy> findAllByEventIdOrderByPriority(@Param("eventId") Long eventId);

    // Buscar top 3 estratégias
    List<PricingStrategy> findTop3ByEventIdAndLifeCycleStateTrueOrderByPriorityDesc(Long eventId);

    // ==================== MÉTODOS TEMPORAIS ====================

    // Buscar estratégias aplicáveis agora (baseado em datas)
    @Query("SELECT ps FROM PricingStrategy ps WHERE ps.event.id = :eventId " +
            "AND ps.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE " +
            "AND ((ps.customStartDate <= :now AND ps.customEndDate >= :now) " +
            "OR (ps.daysBeforeEventStart IS NOT NULL AND ps.daysBeforeEventEnd IS NOT NULL))")
    List<PricingStrategy> findCurrentlyApplicableStrategies(
            @Param("eventId") Long eventId,
            @Param("now") LocalDateTime now
    );

    // Buscar estratégias por período
    List<PricingStrategy> findByCustomStartDateBeforeAndCustomEndDateAfterAndLifeCycleStateTrue(
            LocalDateTime start, LocalDateTime end);

    // ==================== MÉTODOS DE CATEGORIA ====================

    // Buscar estratégias por categoria específica
    @Query("SELECT ps FROM PricingStrategy ps " +
            "WHERE ps.event.id = :eventId " +
            "AND (ps.specificCategory = :category OR ps.specificCategory IS NULL) " +
            "AND ps.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE " +
            "ORDER BY ps.priority DESC")
    List<PricingStrategy> findStrategiesForCategory(
            @Param("eventId") Long eventId,
            @Param("category") TicketCategory category
    );

    // ==================== MÉTODOS DE DEMANDA ====================

    // Buscar estratégias que atingiram threshold de vendas
    @Query("SELECT ps FROM PricingStrategy ps " +
            "WHERE ps.event.id = :eventId " +
            "AND ps.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE " +
            "AND ps.salesThreshold IS NOT NULL " +
            "AND ps.salesThreshold <= :soldPercentage")
    List<PricingStrategy> findStrategiesBySalesThreshold(
            @Param("eventId") Long eventId,
            @Param("soldPercentage") Integer soldPercentage
    );

    // Buscar estratégias baseadas em disponibilidade
    @Query("SELECT ps FROM PricingStrategy ps " +
            "WHERE ps.event.id = :eventId " +
            "AND ps.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE " +
            "AND ps.availableTicketsThreshold IS NOT NULL")
    List<PricingStrategy> findStrategiesWithAvailabilityThreshold(@Param("eventId") Long eventId);

    // ==================== MÉTODOS DE FIDELIDADE ====================

    // Buscar estratégias de fidelidade por tier
    List<PricingStrategy> findByEventIdAndLoyaltyTierAndLifeCycleStateTrue(Long eventId, String tier);

    // Buscar estratégias para primeira compra
    List<PricingStrategy> findByEventIdAndFirstTimeBuyerOnlyTrueAndLifeCycleStateTrue(Long eventId);

    // Buscar estratégias para clientes recorrentes
    List<PricingStrategy> findByEventIdAndRepeatBuyerOnlyTrueAndLifeCycleStateTrue(Long eventId);

    // Buscar estratégias baseadas em volume
    List<PricingStrategy> findByEventIdAndMinPurchasesIsNotNullAndLifeCycleStateTrue(Long eventId);

    // ==================== MÉTODOS DE ESTATÍSTICA ====================

    // Estatísticas de uso das estratégias
    @Query("SELECT ps.strategyType, COUNT(ps), SUM(ps.timesApplied) " +
            "FROM PricingStrategy ps " +
            "WHERE ps.event.id = :eventId " +
            "GROUP BY ps.strategyType")
    List<Object[]> getStrategyStatistics(@Param("eventId") Long eventId);

    // Buscar estratégias mais aplicadas
    List<PricingStrategy> findTop10ByOrderByTimesAppliedDesc();

    // Buscar estratégias que mais geraram receita
    List<PricingStrategy> findTop10ByOrderByTotalRevenueGeneratedDesc();

    // Buscar estratégias que mais deram desconto
    List<PricingStrategy> findTop10ByOrderByTotalDiscountGivenDesc();

    // ==================== MÉTODOS DE BUSCA AVANÇADA ====================

    // Buscar por nome (like)
    List<PricingStrategy> findByEventIdAndStrategyNameContainingIgnoreCase(Long eventId, String StrategyName);

    // Buscar por múltiplos critérios
    @Query("SELECT ps FROM PricingStrategy ps WHERE ps.event.id = :eventId " +
            "AND (:type IS NULL OR ps.strategyType = :type) " +
            "AND (:active IS NULL OR ps.lifeCycleState =  mz.co.mozbuy.common.audit.LifeCycleState.ACTIVE) " +
            "AND (:autoApply IS NULL OR ps.autoApply = :autoApply)")
    List<PricingStrategy> findByFilters(
            @Param("eventId") Long eventId,
            @Param("type") PricingStrategyType type,
            @Param("active") Boolean active,
            @Param("autoApply") Boolean autoApply
    );

    // Contar estratégias ativas por evento
    long countByEventIdAndLifeCycleStateTrue(Long eventId);
}