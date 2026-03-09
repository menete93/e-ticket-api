package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Builder;
import lombok.Data;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;

import java.math.BigDecimal;

@Data
@Builder
public class TicketPriceChangeDTO {
    private Long ticketId;
    private String ticketName;
    private TicketCategory category;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private BigDecimal difference;
    private boolean applied;
}