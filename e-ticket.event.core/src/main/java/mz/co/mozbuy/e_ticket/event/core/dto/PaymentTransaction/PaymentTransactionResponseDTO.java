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
    private String reservationCode;  // ← antes transactionId
    private Long saleId;
    private Long eventId;
    private BigDecimal amount;
    private String status;
    private String paymentMethodCode;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;

    public static PaymentTransactionResponseDTO fromEntity(PaymentTransactionEntity entity) {
        if (entity == null) return null;
        return PaymentTransactionResponseDTO.builder()
                .id(entity.getId())
                .reservationCode(entity.getReservationCode())
                .saleId(entity.getSaleId())
                .eventId(entity.getEventId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .paymentMethodCode(entity.getPaymentMethodCode())
                .createdAt(entity.getCreatedAt())
                .totalAmount(entity.getAmount())
                .build();
    }
}