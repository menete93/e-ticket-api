package mz.co.mozbuy.e_ticket.event.core.dto;



import lombok.Builder;
import mz.co.mozbuy.e_ticket.event.core.enums.PriceAdjustmentType;
import mz.co.mozbuy.e_ticket.event.core.model.ScheduledPriceChange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledPriceChangeResponseDTO {
    private Long id;
    private Long pricingStrategyId;
    private Long eventTicketId;
    private PriceAdjustmentType changeType;
    private BigDecimal changeValue;
    private String pricingStrategyName;
    private BigDecimal newPrice;
    private LocalDateTime scheduledAt;
    private Boolean applyToAllTickets;
    private Boolean isExecuted;
    private LocalDateTime executedAt;
    private String executionResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Validação no setter
    public void setScheduledAt(LocalDateTime scheduledAt) {
        if (scheduledAt == null) {
            throw new IllegalArgumentException("Scheduled date cannot be null");
        }
        this.scheduledAt = scheduledAt;
    }

    public void setChangeValue(BigDecimal changeValue) {
        if (changeValue == null) {
            throw new IllegalArgumentException("Change value cannot be null");
        }
        this.changeValue = changeValue;
    }

    // Método factory estático
    public static ScheduledPriceChangeResponseDTO fromEntity(ScheduledPriceChange entity) {
        ScheduledPriceChangeResponseDTO dto = new ScheduledPriceChangeResponseDTO();
        dto.setId(entity.getId());
        dto.setPricingStrategyId(entity.getPricingStrategy().getId());
        dto.setEventTicketId(entity.getEventTicket() != null ? entity.getEventTicket().getId() : null);
        dto.setChangeType(entity.getChangeType());
        dto.setChangeValue(entity.getChangeValue());
        dto.setNewPrice(entity.getNewPrice());
        dto.setScheduledAt(entity.getScheduledAt());
        dto.setApplyToAllTickets(entity.getApplyToAllTickets());
        dto.setIsExecuted(entity.getIsExecuted());
        dto.setExecutedAt(entity.getExecutedAt());
        dto.setExecutionResult(entity.getExecutionResult());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }
}