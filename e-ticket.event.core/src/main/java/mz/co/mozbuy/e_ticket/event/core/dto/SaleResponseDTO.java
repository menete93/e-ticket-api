package mz.co.mozbuy.e_ticket.event.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SaleResponseDTO {

    private Long id;
    private String transactionId;
    private Long eventId;
    private String eventName;
    private Long ticketId;
    private String ticketName;
    private Long organizerId;
    private String organizerName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal organizerPayout;
    private String couponCode;
    private String buyerEmail;
    private String buyerName;
    private String status;
    private String paymentMethod;
    private Boolean isTrialEvent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static SaleResponseDTO fromEntity(TicketSale sale) {
        SaleResponseDTO dto = new SaleResponseDTO();
        dto.setId(sale.getId());
        dto.setTransactionId(sale.getTransactionId());
        dto.setEventId(sale.getEvent().getId());
        dto.setEventName(sale.getEvent().getName());
        dto.setTicketId(sale.getTicket().getId());
        dto.setTicketName(sale.getTicket().getTicketName());
        dto.setOrganizerId(sale.getOrganizer().getId());
        dto.setOrganizerName(sale.getOrganizer().getName());
        dto.setQuantity(sale.getQuantity());
        dto.setUnitPrice(sale.getUnitPrice());
        dto.setSubtotal(sale.getSubtotal());
        dto.setDiscountAmount(sale.getDiscountAmount());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setCommissionRate(sale.getCommissionRate());
        dto.setCommissionAmount(sale.getCommissionAmount());
        dto.setOrganizerPayout(sale.getOrganizerPayout());
        dto.setCouponCode(sale.getDiscountCoupon() != null ? sale.getDiscountCoupon().getCode() : null);
        dto.setBuyerEmail(sale.getBuyerEmail());
        dto.setBuyerName(sale.getBuyerName());
        dto.setStatus(sale.getStatus().name());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setIsTrialEvent(sale.getIsTrialEvent());
        dto.setSaleDate(sale.getCreatedAt());
        dto.setCreatedAt(sale.getCreatedAt());
        return dto;
    }
}