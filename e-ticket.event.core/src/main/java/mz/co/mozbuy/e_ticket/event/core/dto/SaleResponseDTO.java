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
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponseDTO {
    private Long id;
    private String transactionId;
    private Long eventId;
    private String eventName;

    // ❌ Remover ticketId e ticketName (agora estão nos itens)
    // private Long ticketId;
    // private String ticketName;

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

    // ✅ Itens da venda (múltiplos tickets)
    private List<TicketSaleItemDTO> items;

    // Estratégias aplicadas
    private List<PriceBreakdownItemDTO> appliedStrategies;
    private BigDecimal totalDiscountFromStrategies;

    public static SaleResponseDTO fromEntity(TicketSale sale) {
        SaleResponseDTOBuilder builder = SaleResponseDTO.builder()
                .id(sale.getId())
                .transactionId(sale.getTransactionId())
                .eventId(sale.getEvent() != null ? sale.getEvent().getId() : null)
                .eventName(sale.getEvent() != null ? sale.getEvent().getName() : null)
                .quantity(sale.getQuantity())
                .unitPrice(sale.getUnitPrice())
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .commissionAmount(sale.getCommissionAmount())
                .organizerPayout(sale.getOrganizerPayout())
                .buyerEmail(sale.getBuyerEmail())
                .buyerName(sale.getBuyerName())
                .status(sale.getStatus())
                .createdAt(sale.getCreatedAt())
                .appliedStrategies(sale.getAppliedStrategies())
                .totalDiscountFromStrategies(sale.getTotalDiscountFromStrategies());

        // Adicionar itens (múltiplos tickets)
        if (sale.getItems() != null && !sale.getItems().isEmpty()) {
            builder.items(sale.getItems().stream()
                    .map(TicketSaleItemDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }
}