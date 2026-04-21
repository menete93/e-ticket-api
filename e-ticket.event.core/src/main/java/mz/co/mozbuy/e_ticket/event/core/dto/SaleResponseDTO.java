package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.dto.calculateDto.PriceBreakdownItemDTO;
import mz.co.mozbuy.e_ticket.event.core.enums.SaleStatus;
import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponseDTO {
    private Long id;
    private String transactionId;
    private Long eventId;
    private String eventName;
    private Long ticketId;
    private String ticketName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal commissionAmount;
    private BigDecimal organizerPayout;
    private String buyerEmail;
    private String buyerName;
    private SaleStatus status;
    private LocalDateTime createdAt;

    // 🔥 NOVOS CAMPOS
    private List<PriceBreakdownItemDTO> appliedStrategies;
    private BigDecimal totalDiscountFromStrategies;

    public static SaleResponseDTO fromEntity(TicketSale sale) {
        SaleResponseDTO dto = SaleResponseDTO.builder()
                .id(sale.getId())
                .transactionId(sale.getTransactionId())
                .eventId(sale.getEvent() != null ? sale.getEvent().getId() : null)
                .eventName(sale.getEvent() != null ? sale.getEvent().getName() : null)
                .ticketId(sale.getTicket() != null ? sale.getTicket().getId() : null)
                .ticketName(sale.getTicket() != null ? sale.getTicket().getTicketName() : null)
                .quantity(sale.getQuantity())
                .unitPrice(sale.getTicket().getOriginalPrice())
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .commissionAmount(sale.getCommissionAmount())
                .organizerPayout(sale.getOrganizerPayout())
                .buyerEmail(sale.getBuyerEmail())
                .buyerName(sale.getBuyerName())
                .status(sale.getStatus())
                .createdAt(sale.getCreatedAt())
                // 🔥 NOVOS CAMPOS
                .appliedStrategies(sale.getAppliedStrategies())
                .totalDiscountFromStrategies(sale.getTotalDiscountFromStrategies())
                .build();

        return dto;
    }
}