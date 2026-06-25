package mz.co.mozbuy.e_ticket.event.core.dto;


import lombok.Builder;
import lombok.Data;
import mz.co.mozbuy.e_ticket.event.core.service.payment.PaymentResult;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionStatusResponse {
    private String transactionId;
    private String status;
    private String message;
    private boolean paid;
    private boolean canRetry;
    private int attemptNumber;
    private int maxRetries;
    private LocalDateTime expiresAt;

}