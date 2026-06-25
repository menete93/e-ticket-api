package mz.co.mozbuy.e_ticket.event.core.dto.PaymentResponse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private boolean success;
    private boolean pending;
    private boolean paid;
    private String transactionId;  // ← reservationCode
    private String message;
    private String providerTransactionId;
    private String paymentUrl;
    private String qrCodeUrl;
    private String instructions;
    private LocalDateTime expiresAt;
    private int attemptNumber;
    private int maxRetries;
    private boolean canRetry;
}