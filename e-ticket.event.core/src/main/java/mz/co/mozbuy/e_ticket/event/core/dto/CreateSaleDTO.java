package mz.co.mozbuy.e_ticket.event.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class CreateSaleDTO {

    // ✅ NOVO: para múltiplos tickets (mantém compatibilidade)
    private Map<Long, Integer> ticketQuantities;

    // ✅ Mantém para compatibilidade (um único ticket)
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

    private Long userId;

    private BigDecimal expectedTotalAmount;

    private String utmSource;
    private String utmMedium;
    private String utmCampaign;

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Normaliza as quantidades de tickets (suporta tanto o modelo antigo quanto o novo)
     */
    public Map<Long, Integer> getNormalizedTicketQuantities() {
        Map<Long, Integer> result = new HashMap<>();

        if (ticketQuantities != null && !ticketQuantities.isEmpty()) {
            result.putAll(ticketQuantities);
        }

        if (ticketId != null && quantity != null && quantity > 0) {
            // Se o ticketId já existe no map, soma as quantidades
            result.merge(ticketId, quantity, Integer::sum);
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Nenhum ticket informado");
        }

        return result;
    }

    /**
     * Retorna a quantidade total de ingressos
     */
    public int getTotalQuantity() {
        return getNormalizedTicketQuantities().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Verifica se tem múltiplos tickets diferentes
     */
    public boolean hasMultipleTicketTypes() {
        return getNormalizedTicketQuantities().size() > 1;
    }
}