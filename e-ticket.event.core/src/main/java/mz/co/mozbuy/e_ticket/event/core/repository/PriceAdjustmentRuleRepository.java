package mz.co.mozbuy.e_ticket.event.core.repository;


import mz.co.mozbuy.e_ticket.event.core.enums.PriceAdjustmentType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import mz.co.mozbuy.e_ticket.event.core.model.PriceAdjustmentRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceAdjustmentRuleRepository extends JpaRepository<PriceAdjustmentRule, Long>, JpaSpecificationExecutor<PriceAdjustmentRule> {

    List<PriceAdjustmentRule> findByPricingStrategyId(Long pricingStrategyId);

    List<PriceAdjustmentRule> findByPricingStrategyIdAndIsActiveTrue(Long pricingStrategyId);

    List<PriceAdjustmentRule> findByAdjustmentTypeAndIsActiveTrue(PriceAdjustmentType adjustmentType);

    List<PriceAdjustmentRule> findByApplyToCategoryAndIsActiveTrue(TicketCategory category);

    @Query("SELECT par FROM PriceAdjustmentRule par WHERE par.pricingStrategy.id = :strategyId AND par.isActive = true ORDER BY par.executionOrder ASC")
    List<PriceAdjustmentRule> findActiveRulesByStrategyIdOrdered(@Param("strategyId") Long strategyId);

    @Query("SELECT par FROM PriceAdjustmentRule par WHERE par.pricingStrategy.event.id = :eventId AND par.isActive = true")
    List<PriceAdjustmentRule> findActiveRulesByEventId(@Param("eventId") Long eventId);

    @Query("SELECT MAX(par.executionOrder) FROM PriceAdjustmentRule par WHERE par.pricingStrategy.id = :strategyId")
    Optional<Integer> findMaxExecutionOrderByStrategyId(@Param("strategyId") Long strategyId);

    boolean existsByPricingStrategyIdAndAdjustmentTypeAndIsActiveTrue(Long pricingStrategyId, PriceAdjustmentType adjustmentType);
}