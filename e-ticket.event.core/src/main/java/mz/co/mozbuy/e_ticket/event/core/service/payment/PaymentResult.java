package mz.co.mozbuy.e_ticket.event.core.service.payment;

import lombok.Builder;
import lombok.Data;
import mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction.ProviderResponse;
import mz.co.mozbuy.e_ticket.event.core.model.TicketSale;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResult {

    private final boolean success;
    private final boolean pending;
    private final boolean paid;
    private final String transactionId;
    private final String message;
    private final String errorCode;
    private final String errorCategory;

    private final String providerTransactionId;
    private final String receiptNumber;
    private final LocalDateTime paidAt;

    private final String paymentUrl;
    private final String qrCodeUrl;
    private final String instructions;
    private final LocalDateTime expiresAt;

    private final int attemptNumber;
    private final int maxRetries;
    private final boolean canRetry;

    private final TicketSale sale;

    // ==================== MÉTODOS FACTORY ====================

    public static PaymentResult success(String reservationCode) {
        return PaymentResult.builder()
                .success(true)
                .paid(true)
                .transactionId(reservationCode)
                .paidAt(LocalDateTime.now())
                .message("Pagamento realizado com sucesso")
                .build();
    }

    public static PaymentResult successWithProvider(String transactionId, String providerTransactionId, String receiptNumber) {
        return PaymentResult.builder()
                .success(true)
                .paid(true)
                .transactionId(transactionId)
                .providerTransactionId(providerTransactionId)
                .receiptNumber(receiptNumber)
                .paidAt(LocalDateTime.now())
                .message("Pagamento realizado com sucesso")
                .build();
    }

    public static PaymentResult successWithResponse(String transactionId, ProviderResponse response) {
        return PaymentResult.builder()
                .success(true)
                .paid(true)
                .transactionId(transactionId)
                .providerTransactionId(response.getProviderTransactionId())
                .message(response.getResultMessage())
                .paidAt(LocalDateTime.now())
                .build();
    }

    public static PaymentResult successWithSale(String transactionId, TicketSale sale, ProviderResponse response) {
        return PaymentResult.builder()
                .success(true)
                .paid(true)
                .transactionId(transactionId)
                .sale(sale)
                .providerTransactionId(response.getProviderTransactionId())
                .message(response.getResultMessage())
                .paidAt(LocalDateTime.now())
                .build();
    }

    public static PaymentResult pending(String transactionId, String paymentUrl, String qrCodeUrl,
                                        String instructions, LocalDateTime expiresAt) {
        return PaymentResult.builder()
                .success(false)
                .pending(true)
                .paid(false)
                .transactionId(transactionId)
                .paymentUrl(paymentUrl)
                .qrCodeUrl(qrCodeUrl)
                .instructions(instructions)
                .expiresAt(expiresAt)
                .message("Aguardando confirmação de pagamento")
                .build();
    }

    public static PaymentResult failed(String message, String errorCategory, int attemptNumber,
                                       int maxRetries, boolean canRetry, LocalDateTime expiresAt) {
        return PaymentResult.builder()
                .success(false)
                .pending(false)
                .paid(false)
                .message(message)
                .errorCategory(errorCategory)
                .attemptNumber(attemptNumber)
                .maxRetries(maxRetries)
                .canRetry(canRetry)
                .expiresAt(expiresAt)
                .build();
    }

    public static PaymentResult failed(String message) {
        return PaymentResult.builder()
                .success(false)
                .pending(false)
                .paid(false)
                .message(message)
                .canRetry(false)
                .build();
    }

    public static PaymentResult blocked(String reason) {
        return PaymentResult.builder()
                .success(false)
                .pending(false)
                .paid(false)
                .message(reason)
                .errorCategory("BLOCKED")
                .canRetry(false)
                .build();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    public boolean isSuccess() { return success; }
    public boolean isPending() { return pending; }
    public boolean isFailed() { return !success && !pending; }
    public boolean isPaid() { return paid; }
    public boolean hasRetry() { return canRetry && !success && !paid; }

    // ✅ MÉTODO ADICIONADO - verifica se a reserva expirou
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}