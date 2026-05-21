// dto/PaymentTransaction/PaymentTransactionResponseDTO.java
package mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionResponseDTO {
    private Long id;
    private String transactionId;
    private Long saleId;
    private Long eventId;
    private BigDecimal amount;
    private String status;
    private String payerPhone;
    private String payerEmail;
    private String payerName;
    private LocalDateTime createdAt;

    // 🔥 NOVOS CAMPOS DOS RELACIONAMENTOS
    private String eventName;
    private String ticketName;
    private Integer quantity;
    private BigDecimal totalAmount;

    // 🔥 MÉTODO ESTÁTICO PARA CONVERTER DA ENTIDADE
    public static PaymentTransactionResponseDTO fromEntity(PaymentTransactionEntity entity) {
        if (entity == null) return null;

        return PaymentTransactionResponseDTO.builder()
                .id(entity.getId())
                .transactionId(entity.getTransactionId())
                .saleId(entity.getSaleId())
                .eventId(entity.getEventId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .payerPhone(entity.getPayerPhone())
                .payerEmail(entity.getPayerEmail())
                .payerName(entity.getPayerName())
                .createdAt(entity.getCreatedAt())
                // 🔥 BUSCANDO DOS RELACIONAMENTOS
                .eventName(entity.getEventName())
                .ticketName(entity.getTicketName())
                .quantity(entity.getQuantity())
                .totalAmount(entity.getAmount())
                .build();
    }
}