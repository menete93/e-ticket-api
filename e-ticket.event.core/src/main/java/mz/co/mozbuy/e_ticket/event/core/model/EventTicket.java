package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.common.converter.StringListConverter;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event_tickets")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventTicket extends AuditableEntity<Long, String> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketCategory category;

    @Column(name = "ticket_name", nullable = false, length = 100)
    private String ticketName;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Builder.Default
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Builder.Default
    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity = 0;

    @Column(name = "current_price", precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "original_price", precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @Column(length = 1000)
    private String description;

    @Column(name = "benefits", length = 1000)
    @Convert(converter = StringListConverter.class)
    private List<String> benefits;

    @Column(name = "sales_start_date")
    private LocalDateTime salesStartDate;

    @Column(name = "sales_end_date")
    private LocalDateTime salesEndDate;

    @Builder.Default
    @Column(name = "max_tickets_per_user")
    private Integer maxTicketsPerUser = 10;

    @Column(name = "has_dynamic_pricing", nullable = false)
    private Boolean hasDynamicPricing = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_strategy_id")
    private PricingStrategy pricingStrategy;

    @OneToMany(mappedBy = "eventTicket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduledPriceChange> scheduledPriceChanges = new ArrayList<>();

    @OneToMany(mappedBy = "eventTicket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketPriceHistory> priceHistory = new ArrayList<>();

    public boolean isAvailable() {
        return this.getLifeCycleState().equals(LifeCycleState.ACTIVE) && availableQuantity > 0 && isSalesPeriodActive();
    }

    public boolean isSalesPeriodActive() {
        LocalDateTime now = LocalDateTime.now();
        return (salesStartDate == null || now.isAfter(salesStartDate)) &&
                (salesEndDate == null || now.isBefore(salesEndDate));
    }

    // NOVO MÉTODO NO EventTicket (adicione à classe existente)
    public void updatePrice(BigDecimal newPrice, String reason, String changeType, Long strategyId) {
        if (this.currentPrice != null && !this.currentPrice.equals(newPrice)) {
            // Criar histórico
            TicketPriceHistory history = new TicketPriceHistory();
            history.setEventTicket(this);
            history.setOldPrice(this.currentPrice);
            history.setNewPrice(newPrice);
            history.setChangeReason(reason);
            history.setChangedAt(LocalDateTime.now());
            history.setChangeType(changeType);
            history.setStrategyId(strategyId);

            if (this.priceHistory == null) {
                this.priceHistory = new ArrayList<>();
            }
            this.priceHistory.add(history);
        }
        this.currentPrice = newPrice;
    }
    public void reserveTicket(Integer quantity) {
        if (availableQuantity >= quantity) {
            availableQuantity -= quantity;
            reservedQuantity += quantity;
        } else {
            throw new IllegalStateException("Not enough tickets available for reservation");
        }
    }

    public void confirmSale(Integer quantity) {
        if (reservedQuantity >= quantity) {
            reservedQuantity -= quantity;
            soldQuantity += quantity;
        } else {
            throw new IllegalStateException("Not enough reserved tickets for sale confirmation");
        }
    }

    public void releaseReservation(Integer quantity) {
        if (reservedQuantity >= quantity) {
            reservedQuantity -= quantity;
            availableQuantity += quantity;
        } else {
            throw new IllegalStateException("Not enough reserved tickets to release");
        }
    }

    public BigDecimal getTotalRevenue() {
        return currentPrice != null ? currentPrice.multiply(BigDecimal.valueOf(soldQuantity)) : BigDecimal.ZERO;
    }

    public Double getSoldPercentage() {
        return totalQuantity > 0 ? (soldQuantity.doubleValue() / totalQuantity.doubleValue()) * 100 : 0.0;
    }

    private BigDecimal getBasePrice(EventTicket ticket, PricingStrategy strategy) {
        // Para estratégias de demanda/tiragem, o mais lógico é usar o preço original
        // como referência, pois ele representa o valor "base" do ingresso

        BigDecimal basePrice;

        // 1. Tenta usar o preço original do ticket
        if (ticket.getOriginalPrice() != null) {
            basePrice = ticket.getOriginalPrice();
        }
        // 2. Se não tem original, usa o current (pode já ter sido alterado)
        else {
            basePrice = ticket.getCurrentPrice();
        }

        return basePrice;
    }
}