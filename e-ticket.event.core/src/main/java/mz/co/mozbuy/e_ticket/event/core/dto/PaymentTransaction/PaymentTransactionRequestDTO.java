package mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionRequestDTO {
    private Long saleId;
    private Long eventId;
    private Long userId;
    private BigDecimal amount;
    private String payerPhone;
    private String payerEmail;
    private String payerName;
}