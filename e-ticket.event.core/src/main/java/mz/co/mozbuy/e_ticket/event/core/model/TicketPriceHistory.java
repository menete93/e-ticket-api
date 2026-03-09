package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mz.co.mozbuy.common.audit.AuditableEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_price_history")
@Getter
@Setter
public class TicketPriceHistory extends AuditableEntity<Long, String> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_ticket_id", nullable = false)
    private EventTicket eventTicket;

    @Column(name = "old_price", precision = 15, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "new_price", precision = 15, scale = 2)
    private BigDecimal newPrice;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "change_type", length = 50)
    private String changeType; // MANUAL, SCHEDULED, DYNAMIC, AUTO

    @Column(name = "strategy_id")
    private Long strategyId; // ID da estratégia que causou a mudança

    @Column(name = "scheduled_change_id")
    private Long scheduledChangeId; // Se veio de uma mudança programada

    // Método de conveniência para criar histórico
    public static TicketPriceHistory createHistory(EventTicket ticket, BigDecimal oldPrice,
                                                   String reason, String changeType,
                                                   Long strategyId) {
        TicketPriceHistory history = new TicketPriceHistory();
        history.setEventTicket(ticket);
        history.setOldPrice(oldPrice);
        history.setNewPrice(ticket.getCurrentPrice());
        history.setChangeReason(reason);
        history.setChangedAt(LocalDateTime.now());
        history.setChangeType(changeType);
        history.setStrategyId(strategyId);
        return history;
    }
}