package mz.co.mozbuy.e_ticket.event.core.dto.PaymentRequest;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long saleId;
    private Long eventId;
    private Long userId;
    private String providerCode;
    private BigDecimal amount;
    private String phoneNumber;
    private String email;
    private String name;
}