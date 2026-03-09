package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Builder;
import lombok.Data;
import mz.co.mozbuy.e_ticket.event.core.enums.PricingStrategyType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PricingStrategyEffectivenessDTO {
    private Long strategyId;
    private String strategyName;
    private PricingStrategyType strategyType;
    private Integer timesApplied;
    private BigDecimal totalDiscountGiven;
    private BigDecimal totalRevenueGenerated;
    private LocalDateTime lastAppliedAt;
    private Double averageDiscountPerApplication;
    private Double averageRevenuePerApplication;
}