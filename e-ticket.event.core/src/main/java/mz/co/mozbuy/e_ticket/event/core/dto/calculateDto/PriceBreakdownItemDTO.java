package mz.co.mozbuy.e_ticket.event.core.dto.calculateDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.enums.TicketCategory;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdownItemDTO {
    private String type; // "TICKET" ou "STRATEGY"
    private Long ticketId;
    private String ticketName;
    private TicketCategory category;
    private BigDecimal basePrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private String name; // nome da estratégia
    private String description;
    private BigDecimal discountValue;
}