package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.e_ticket.event.core.enums.PriceAdjustmentType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_adjustment_rules")
@Getter
@Setter
public class PriceAdjustmentRule extends AuditableEntity<Long, String> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_strategy_id", nullable = false)
    private PricingStrategy pricingStrategy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PriceAdjustmentType adjustmentType;

    @Column(name = "adjustment_value", precision = 10, scale = 2)
    private BigDecimal adjustmentValue;

    @Column(name = "trigger_threshold")
    private Integer triggerThreshold; // Porcentagem de vendas ou dias até o evento

    @Column(name = "apply_to_category")
    @Enumerated(EnumType.STRING)
    private TicketCategory applyToCategory;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "execution_order", nullable = false)
    private Integer executionOrder = 1;

    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;
}
