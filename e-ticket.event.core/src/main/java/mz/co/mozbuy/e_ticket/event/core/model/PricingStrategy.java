package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_strategies")
@Getter
@Setter
public class PricingStrategy extends AuditableEntity<Long, String> {

    @Column(name = "strategy_name", nullable = false, length = 100)
    private String strategyName;

    @Column(name = "strategy_type", nullable = false, length = 50)
    private String strategyType; // FIXED, DYNAMIC, TIERED, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "min_price", precision = 15, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 15, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "demand_multiplier", precision = 5, scale = 2)
    private BigDecimal demandMultiplier;

    @Column(name = "time_based_increase_days")
    private Integer timeBasedIncreaseDays;

    @Column(name = "time_based_increase_percentage", precision = 5, scale = 2)
    private BigDecimal timeBasedIncreasePercentage;

    @Column(name = "group_size_threshold")
    private Integer groupSizeThreshold;

    @Column(name = "group_discount_percentage", precision = 5, scale = 2)
    private BigDecimal groupDiscountPercentage;

    @Column(name = "demand_threshold_percentage", precision = 5, scale = 2)
    private BigDecimal demandThresholdPercentage;

    @Column(name = "price_increase_percentage", precision = 5, scale = 2)
    private BigDecimal priceIncreasePercentage;

    @Column(name = "apply_automatically", nullable = false)
    private Boolean applyAutomatically = false;

    @Column(name = "last_applied_at")
    private LocalDateTime lastAppliedAt;

    @Column(name = "description", length = 500)
    private String description;

    public PricingStrategy() {}

    public PricingStrategy(String strategyName, String strategyType, Event event) {
        this.strategyName = strategyName;
        this.strategyType = strategyType;
        this.event = event;
    }

    public boolean canApplyAutomatically() {
        return this.getLifeCycleState().equals(LifeCycleState.ACTIVE) && applyAutomatically;
    }

    public boolean hasPriceLimits() {
        return minPrice != null || maxPrice != null;
    }

    public boolean isWithinPriceLimits(BigDecimal price) {
        if (minPrice != null && price.compareTo(minPrice) < 0) {
            return false;
        }
        return maxPrice == null || price.compareTo(maxPrice) <= 0;
    }
}