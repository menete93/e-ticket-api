package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Data;
import lombok.Builder;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StrategyApplicationResultDTO {
    private Long strategyId;
    private String strategyName;
    private LocalDateTime appliedAt;
    private Integer ticketsAffected;
    private List<TicketPriceChangeDTO> priceChanges;
    private BigDecimal totalDiscountApplied;
    private String status; // SUCCESS, PARTIAL, FAILED
    private String message;
}

