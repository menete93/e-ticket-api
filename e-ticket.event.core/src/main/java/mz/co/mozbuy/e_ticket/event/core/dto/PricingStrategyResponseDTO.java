package mz.co.mozbuy.e_ticket.event.core.dto;


import mz.co.mozbuy.e_ticket.event.core.model.PricingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingStrategyResponseDTO {
    private Long id;
    private String strategyName;
    private String strategyType;
    private Long eventId;
    private BigDecimal basePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal demandMultiplier;
    private Integer timeBasedIncreaseDays;
    private BigDecimal timeBasedIncreasePercentage;
    private Integer groupSizeThreshold;
    private BigDecimal groupDiscountPercentage;
    private BigDecimal demandThresholdPercentage;
    private BigDecimal priceIncreasePercentage;
    private Boolean isActive;
    private Boolean applyAutomatically;
    private LocalDateTime lastAppliedAt;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Método factory estático
    public static PricingStrategyResponseDTO fromEntity(PricingStrategy strategy) {
        PricingStrategyResponseDTO dto = new PricingStrategyResponseDTO();
        dto.setId(strategy.getId());
        dto.setStrategyName(strategy.getStrategyName());
        dto.setStrategyType(strategy.getStrategyType());
        dto.setEventId(strategy.getEvent().getId());
        dto.setBasePrice(strategy.getBasePrice());
        dto.setMinPrice(strategy.getMinPrice());
        dto.setMaxPrice(strategy.getMaxPrice());
        dto.setDemandMultiplier(strategy.getDemandMultiplier());
        dto.setTimeBasedIncreaseDays(strategy.getTimeBasedIncreaseDays());
        dto.setTimeBasedIncreasePercentage(strategy.getTimeBasedIncreasePercentage());
        dto.setGroupSizeThreshold(strategy.getGroupSizeThreshold());
        dto.setGroupDiscountPercentage(strategy.getGroupDiscountPercentage());
        dto.setDemandThresholdPercentage(strategy.getDemandThresholdPercentage());
        dto.setPriceIncreasePercentage(strategy.getPriceIncreasePercentage());
        dto.setIsActive(strategy.getIsActive());
        dto.setApplyAutomatically(strategy.getApplyAutomatically());
        dto.setLastAppliedAt(strategy.getLastAppliedAt());
        dto.setDescription(strategy.getDescription());
        dto.setCreatedAt(strategy.getCreatedAt());
        dto.setUpdatedAt(strategy.getUpdatedAt());
        dto.setCreatedBy(strategy.getCreatedBy());
        dto.setUpdatedBy(strategy.getUpdatedBy());
        return dto;
    }
}