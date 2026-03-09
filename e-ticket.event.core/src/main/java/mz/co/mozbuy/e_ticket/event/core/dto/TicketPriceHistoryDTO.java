package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketPriceHistoryDTO {
    private Long id;
    private Long eventTicketId;
    private String eventTicketName;
    private Long eventId;
    private String eventName;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String changeReason;
    private LocalDateTime changedAt;
    private String changeType; // MANUAL, SCHEDULED, DYNAMIC, etc.
    private Long strategyId;
    private String strategyName;

    // Construtor vazio necessário para algumas frameworks
    public TicketPriceHistoryDTO() {}

    public TicketPriceHistoryDTO(Long id, Long eventTicketId, String eventTicketName,
                                 Long eventId, String eventName, BigDecimal oldPrice,
                                 BigDecimal newPrice, String changeReason, LocalDateTime changedAt,
                                 String changeType, Long strategyId, String strategyName) {
        this.id = id;
        this.eventTicketId = eventTicketId;
        this.eventTicketName = eventTicketName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.changeReason = changeReason;
        this.changedAt = changedAt;
        this.changeType = changeType;
        this.strategyId = strategyId;
        this.strategyName = strategyName;
    }
}