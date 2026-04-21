package mz.co.mozbuy.e_ticket.event.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSaleDTO {

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    private String couponCode;

    @Email(message = "Invalid email format")
    private String buyerEmail;

    private String buyerName;

    private String buyerPhone;

    private String paymentMethod;

    // 🔥 NOVO: ID do usuário (vem do token JWT no front-end)
    private Long userId;

    // 🔥 OPCIONAL: Para garantir que o preço calculado no front-end
    // corresponde ao que o backend calculou
    private BigDecimal expectedTotalAmount;

    // 🔥 OPCIONAL: Para rastrear de onde veio a compra
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
}