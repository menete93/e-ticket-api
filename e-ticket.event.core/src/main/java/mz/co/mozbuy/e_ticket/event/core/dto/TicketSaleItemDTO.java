package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Builder;
import lombok.Data;
import mz.co.mozbuy.e_ticket.event.core.model.TicketSaleItem;
import java.math.BigDecimal;

@Data
@Builder
public class TicketSaleItemDTO {
    private Long id;
    private Long saleId;
    private Long ticketId;
    private String ticketName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal finalPrice;
    private BigDecimal discountAmount;

    public static TicketSaleItemDTO fromEntity(TicketSaleItem item) {
        if (item == null) return null;

        return TicketSaleItemDTO.builder()
                .id(item.getId())
                .saleId(item.getSale() != null ? item.getSale().getId() : null)
                .ticketId(item.getTicket() != null ? item.getTicket().getId() : null)
                .ticketName(item.getTicket() != null ? item.getTicket().getTicketName() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
//                .subtotal(item.getSubtotal())
                .finalPrice(item.getFinalPrice())
                .discountAmount(item.getDiscountAmount())
                .build();
    }
}