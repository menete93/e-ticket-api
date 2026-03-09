package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Data;
import lombok.Builder;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PricingStrategyResponseDTO {
    private Long id;
    private String name;
    private PricingStrategyType strategyType;
    private Long eventId;
    private String eventName;
    private TicketCategory specificCategory;

    // Configurações temporais
    private Integer daysBeforeEventStart;
    private Integer daysBeforeEventEnd;
    private LocalDateTime customStartDate;
    private LocalDateTime customEndDate;

    // Configurações de demanda
    private Integer salesThreshold;
    private Integer availableTicketsThreshold;

    // Ajustes de preço
    private BigDecimal percentageAdjustment;
    private BigDecimal fixedAdjustment;
    private BigDecimal multiplier;

    // Limites
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Regras de grupo
    private Integer minGroupSize;
    private BigDecimal groupDiscountPercentage;

    // 🆕 NOVOS CAMPOS DE FIDELIDADE
    private String loyaltyTier;
    private Integer minPurchases;
    private BigDecimal minTotalSpent;
    private boolean firstTimeBuyerOnly;
    private boolean repeatBuyerOnly;
    private boolean exclusiveToTier;
    private String description;

    // Status e métricas
    private Integer priority;
    private boolean active;
    private boolean autoApply;
    private LocalDateTime lastAppliedAt;
    private Integer timesApplied;
    private BigDecimal totalDiscountGiven;
    private BigDecimal totalRevenueGenerated;

    // Auditoria
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    // Métricas calculadas
    private boolean currentlyApplicable;
    private Long affectedTicketsCount;
}