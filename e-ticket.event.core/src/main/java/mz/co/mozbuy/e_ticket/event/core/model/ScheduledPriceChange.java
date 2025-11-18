package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.e_ticket.event.core.enums.PriceAdjustmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_price_changes")
@Getter
@Setter
public class ScheduledPriceChange extends AuditableEntity<Long, String> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_strategy_id", nullable = false)
    private PricingStrategy pricingStrategy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_ticket_id")
    private EventTicket eventTicket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PriceAdjustmentType changeType;

    @Column(name = "change_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal changeValue;

    @Column(name = "new_price", precision = 15, scale = 2)
    private BigDecimal newPrice;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "is_executed", nullable = false)
    private Boolean isExecuted = false;

    @Column(name = "execution_result", length = 500)
    private String executionResult;

    @Column(name = "apply_to_all_tickets", nullable = false)
    private Boolean applyToAllTickets = false;
}