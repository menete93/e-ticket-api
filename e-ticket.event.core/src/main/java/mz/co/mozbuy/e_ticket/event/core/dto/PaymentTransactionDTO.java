package mz.co.mozbuy.e_ticket.event.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionDTO {
    private Long id;
    private String transactionId;
    private String externalId;
    private Long saleId;
    private Long eventId;
    private Long userId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String payerName;
    private String payerEmail;
    private String payerPhone;
    private String providerCheckoutId;
    private String providerTransactionId;
    private Integer providerResultCode;
    private String providerResultDesc;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String state;
}