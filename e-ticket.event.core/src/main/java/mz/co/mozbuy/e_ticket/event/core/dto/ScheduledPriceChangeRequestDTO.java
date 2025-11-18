package mz.co.mozbuy.e_ticket.event.core.dto;



import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.enums.PriceAdjustmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledPriceChangeRequestDTO {

    @NotNull(message = "Pricing strategy ID is required")
    private Long pricingStrategyId;

    private Long eventTicketId;

    @NotNull(message = "Change type is required")
    private PriceAdjustmentType changeType;

    @NotNull(message = "Change value is required")
    private BigDecimal changeValue;

    private BigDecimal newPrice;

    @NotNull(message = "Scheduled date is required")
    private LocalDateTime scheduledAt;

    private Boolean applyToAllTickets = false;
}