package mz.co.mozbuy.e_ticket.event.core.dto.PaymentTransaction;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
public class ProviderResponse {

    // Status flags
    private boolean success;
    private boolean pending;

    // Dados comuns
    private String rawResponse;
    private String providerTransactionId;

    // Sucesso
    private String resultCode;
    private String resultMessage;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private String receiptUrl;

    // Pendente
    private String pendingType;
    private String paymentUrl;
    private String qrCodeUrl;
    private String instructions;
    private LocalDateTime expiresAt;

    // Falha
    private String errorMessage;
    private String failureCategory;
    private String failureCode;
    private boolean canRetry;

    // Payloads
    private Map<String, Object> requestPayload;

    // ==================== FACTORY METHODS ====================

    public static ProviderResponse success(String providerTransactionId, String resultCode, String resultMessage) {
        return ProviderResponse.builder()
                .success(true)
                .pending(false)
                .providerTransactionId(providerTransactionId)
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .build();
    }

    public static ProviderResponse success(String providerTransactionId, String resultCode,
                                           String resultMessage, Map<String, Object> rawResponse) {
        return ProviderResponse.builder()
                .success(true)
                .pending(false)
                .providerTransactionId(providerTransactionId)
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .rawResponse(rawResponse != null ? rawResponse.toString() : null)
                .build();
    }

    public static ProviderResponse pending(String pendingType, String paymentUrl, String qrCodeUrl,
                                           String instructions, LocalDateTime expiresAt) {
        return ProviderResponse.builder()
                .success(false)
                .pending(true)
                .pendingType(pendingType)
                .paymentUrl(paymentUrl)
                .qrCodeUrl(qrCodeUrl)
                .instructions(instructions)
                .expiresAt(expiresAt)
                .build();
    }

    public static ProviderResponse failed(String errorMessage, String failureCategory, boolean canRetry) {
        return ProviderResponse.builder()
                .success(false)
                .pending(false)
                .errorMessage(errorMessage)
                .failureCategory(failureCategory)
                .canRetry(canRetry)
                .build();
    }

    public static ProviderResponse failed(String errorMessage, String failureCategory,
                                          boolean canRetry, Map<String, Object> rawResponse) {
        return ProviderResponse.builder()
                .success(false)
                .pending(false)
                .errorMessage(errorMessage)
                .failureCategory(failureCategory)
                .canRetry(canRetry)
                .rawResponse(rawResponse != null ? rawResponse.toString() : null)
                .build();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    public boolean isSuccess() {
        return success;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isFailed() {
        return !success && !pending;
    }
}