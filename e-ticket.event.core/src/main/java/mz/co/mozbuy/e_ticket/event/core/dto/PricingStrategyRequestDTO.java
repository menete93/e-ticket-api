package mz.co.mozbuy.e_ticket.event.core.dto;



import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingStrategyRequestDTO {

    @NotNull(message = "Strategy name is required")
    private String strategyName;
    

    @NotNull(message = "Strategy type is required")
    private String strategyType; // "FIXED", "DYNAMIC", "TIERED", etc.

    @NotNull(message = "Event ID is required")
    private Long eventId;

    private BigDecimal basePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal demandMultiplier;
    private Integer timeBasedIncreaseDays;
    private BigDecimal timeBasedIncreasePercentage;
    private Integer groupSizeThreshold;
    private BigDecimal groupDiscountPercentage;

    // ✅ ADICIONE ESTES CAMPOS QUE ESTAVAM FALTANDO
    private BigDecimal demandThresholdPercentage;
    private BigDecimal priceIncreasePercentage;
    private String description;

    private Boolean isActive = true;
    private Boolean applyAutomatically = false;
}