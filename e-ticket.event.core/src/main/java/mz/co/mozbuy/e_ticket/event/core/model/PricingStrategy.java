package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import mz.co.mozbuy.common.audit.AuditableEntity;
import mz.co.mozbuy.common.audit.LifeCycleState;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pricing_strategies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingStrategy extends AuditableEntity<Long, String> {

    @Column(name = "strategy_name", nullable = false, length = 100)
    private String strategyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy_type", nullable = false, length = 50)
    private PricingStrategyType strategyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "specific_category", length = 50)
    private TicketCategory specificCategory;

    // ⏰ CONFIGURAÇÕES TEMPORAIS
    @Column(name = "days_before_event_start")
    private Integer daysBeforeEventStart;

    @Column(name = "days_before_event_end")
    private Integer daysBeforeEventEnd;

    @Column(name = "custom_start_date")
    private LocalDateTime customStartDate;

    @Column(name = "custom_end_date")
    private LocalDateTime customEndDate;

    @Column(name = "description", length = 500)
    private String description;

    // 📊 CONFIGURAÇÕES DE DEMANDA
    @Column(name = "sales_threshold")
    private Integer salesThreshold;

    @Column(name = "available_tickets_threshold")
    private Integer availableTicketsThreshold;

    // 💰 AJUSTES DE PREÇO
    @Column(name = "percentage_adjustment", precision = 5, scale = 2)
    private BigDecimal percentageAdjustment;

    @Column(name = "fixed_adjustment", precision = 15, scale = 2)
    private BigDecimal fixedAdjustment;

    @Column(name = "multiplier", precision = 5, scale = 2)
    private BigDecimal multiplier;

    // 🎯 LIMITES
    @Column(name = "min_price", precision = 15, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 15, scale = 2)
    private BigDecimal maxPrice;

    // 👥 REGRAS DE GRUPO
    @Column(name = "min_group_size")
    private Integer minGroupSize;

    @Column(name = "group_discount_percentage", precision = 5, scale = 2)
    private BigDecimal groupDiscountPercentage;

    // 📦 CONFIGURAÇÕES DE BUNDLE (NOVO)
    @ElementCollection
    @CollectionTable(
            name = "strategy_bundle_tickets",
            joinColumns = @JoinColumn(name = "strategy_id")
    )
    @Column(name = "ticket_id")
    @Builder.Default
    private List<Long> bundleTicketIds = new ArrayList<>();

    @Column(name = "bundle_discount_percentage", precision = 5, scale = 2)
    private BigDecimal bundleDiscountPercentage;

    // 🎟️ CONFIGURAÇÕES DE APLICAÇÃO
    @Column(nullable = false)
    private Integer priority;

    @Column(name = "auto_apply", nullable = false)
    private boolean autoApply;

    // 📈 MÉTRICAS
    @Column(name = "last_applied_at")
    private LocalDateTime lastAppliedAt;

    @Column(name = "times_applied")
    private Integer timesApplied;

    @Column(name = "total_discount_given", precision = 15, scale = 2)
    private BigDecimal totalDiscountGiven;

    @Column(name = "total_revenue_generated", precision = 15, scale = 2)
    private BigDecimal totalRevenueGenerated;

    // 🎯 REGRAS DE FIDELIDADE
    @Column(name = "loyalty_tier", length = 20)
    private String loyaltyTier; // BRONZE, SILVER, GOLD, PLATINUM

    @Column(name = "min_purchases")
    private Integer minPurchases; // Mínimo de compras anteriores

    @Column(name = "min_total_spent", precision = 15, scale = 2)
    private BigDecimal minTotalSpent; // Mínimo gasto

    @Column(name = "first_time_buyer_only")
    private boolean firstTimeBuyerOnly = false; // Apenas para primeira compra

    @Column(name = "repeat_buyer_only")
    private boolean repeatBuyerOnly = false; // Apenas para compras repetidas

    @Column(name = "exclusive_to_tier")
    private boolean exclusiveToTier = false; // Exclusivo para um tier específico

    // ✅ MÉTODOS DE NEGÓCIO

    /**
     * Verifica se a estratégia é aplicável no momento atual
     */
    public boolean isCurrentlyApplicable() {
        if (!this.getState().equals(LifeCycleState.ACTIVE)) return false;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventDate = event.getEventDate();

        // Se evento já passou, não aplica
        if (eventDate != null && now.isAfter(eventDate)) return false;

        // Verificar período baseado em dias antes do evento
        if (daysBeforeEventStart != null && daysBeforeEventEnd != null && eventDate != null) {
            long daysToEvent = Duration.between(now, eventDate).toDays();
            return daysToEvent >= daysBeforeEventEnd && daysToEvent <= daysBeforeEventStart;
        }

        // Verificar período customizado
        if (customStartDate != null && customEndDate != null) {
            return !now.isBefore(customStartDate) && !now.isAfter(customEndDate);
        }

        return true; // Sempre aplicável se ativa
    }

    /**
     * Verifica se a estratégia é acionada por demanda/vendas
     */
    public boolean isTriggeredByDemand(EventTicket ticket) {
        if (salesThreshold != null && ticket != null) {
            double soldPercentage = (ticket.getSoldQuantity() * 100.0) / ticket.getTotalQuantity();
            return soldPercentage >= salesThreshold;
        }
        return false;
    }

    /**
     * Verifica se é acionada por disponibilidade
     */
    public boolean isTriggeredByAvailability(EventTicket ticket) {
        if (availableTicketsThreshold != null && ticket != null) {
            return ticket.getAvailableQuantity() <= availableTicketsThreshold;
        }
        return false;
    }

    /**
     * Calcula o preço ajustado baseado na estratégia
     */
    public BigDecimal calculateAdjustedPrice(BigDecimal originalPrice) {
        if (originalPrice == null) return null;

        BigDecimal adjustedPrice = originalPrice;

        if (percentageAdjustment != null) {
            BigDecimal adjustmentFactor = BigDecimal.ONE
                    .add(percentageAdjustment.divide(BigDecimal.valueOf(100)));
            adjustedPrice = adjustedPrice.multiply(adjustmentFactor);
        }

        if (fixedAdjustment != null) {
            adjustedPrice = adjustedPrice.add(fixedAdjustment);
        }

        if (multiplier != null) {
            adjustedPrice = originalPrice.multiply(multiplier);
        }

        // Aplicar limites
        if (minPrice != null && adjustedPrice.compareTo(minPrice) < 0) {
            adjustedPrice = minPrice;
        }
        if (maxPrice != null && adjustedPrice.compareTo(maxPrice) > 0) {
            adjustedPrice = maxPrice;
        }

        return adjustedPrice.max(BigDecimal.ZERO);
    }

    /**
     * Verifica se a estratégia se aplica a um ticket específico
     */
    public boolean appliesToTicket(EventTicket ticket) {
        if (ticket == null) return false;

        // Se tem categoria específica, só aplica a essa categoria
        if (specificCategory != null) {
            return specificCategory.equals(ticket.getCategory());
        }

        // Se é estratégia de bundle, verifica se o ticket está no bundle
        if (strategyType == PricingStrategyType.BUNDLE ||
                strategyType == PricingStrategyType.BUNDLE_DISCOUNT) {
            return bundleTicketIds != null && bundleTicketIds.contains(ticket.getId());
        }

        return true; // Aplica a todas categorias
    }

    /**
     * Calcula o preço do bundle baseado nos tickets incluídos
     */
    public BigDecimal calculateBundlePrice(List<EventTicket> tickets) {
        if (tickets == null || tickets.isEmpty()) return BigDecimal.ZERO;

        // Soma o preço de todos os tickets
        BigDecimal totalOriginal = tickets.stream()
                .map(EventTicket::getCurrentPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Aplica desconto do bundle
        if (bundleDiscountPercentage != null) {
            BigDecimal discountMultiplier = BigDecimal.ONE
                    .subtract(bundleDiscountPercentage.divide(BigDecimal.valueOf(100)));
            return totalOriginal.multiply(discountMultiplier);
        }

        // Aplica desconto percentual geral se existir
        if (percentageAdjustment != null) {
            return calculateAdjustedPrice(totalOriginal);
        }

        return totalOriginal;
    }

    /**
     * Registra uma aplicação da estratégia
     */
    public void recordApplication(BigDecimal discountAmount, BigDecimal revenueAmount) {
        this.lastAppliedAt = LocalDateTime.now();
        this.timesApplied = (this.timesApplied == null ? 1 : this.timesApplied + 1);

        if (discountAmount != null) {
            this.totalDiscountGiven = (this.totalDiscountGiven == null ?
                    discountAmount : this.totalDiscountGiven.add(discountAmount));
        }

        if (revenueAmount != null) {
            this.totalRevenueGenerated = (this.totalRevenueGenerated == null ?
                    revenueAmount : this.totalRevenueGenerated.add(revenueAmount));
        }
    }

    /**
     * Valida se a estratégia tem configurações válidas para seu tipo
     */
    public boolean isValidForType() {
        switch (strategyType) {
            case BUNDLE:
            case BUNDLE_DISCOUNT:
                return bundleTicketIds != null && !bundleTicketIds.isEmpty() &&
                        (bundleDiscountPercentage != null || percentageAdjustment != null);

            case GROUP_DISCOUNT:
                return minGroupSize != null && minGroupSize > 0 &&
                        (groupDiscountPercentage != null || percentageAdjustment != null);

            case EARLY_BIRD:
            case LAST_MINUTE:
            case TIME_BASED:
                return (daysBeforeEventStart != null && daysBeforeEventEnd != null) ||
                        (customStartDate != null && customEndDate != null);

            case DEMAND_BASED:
                return salesThreshold != null || availableTicketsThreshold != null;

            case LOYALTY:
            case LOYALTY_DISCOUNT:
                return loyaltyTier != null || minPurchases != null ||
                        minTotalSpent != null || firstTimeBuyerOnly || repeatBuyerOnly;

            case FIRST_BUYER:
                return firstTimeBuyerOnly;

            case VOLUME_BASED:
                return minPurchases != null && minPurchases > 0;

            case TIERED_PRICING:
                return minPrice != null && maxPrice != null;

            case CATEGORY_SPECIFIC:
                return specificCategory != null;

            default:
                return true;
        }
    }
}