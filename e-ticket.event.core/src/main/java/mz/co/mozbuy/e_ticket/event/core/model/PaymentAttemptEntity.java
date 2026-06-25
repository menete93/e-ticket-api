package mz.co.mozbuy.e_ticket.event.core.model;

import jakarta.persistence.*;
import lombok.*;
import mz.co.mozbuy.common.audit.AuditableEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_attempt", schema = "e_ticket")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAttemptEntity extends AuditableEntity<Long, String> {


    @Column(name = "payment_transaction_id", nullable = false)
    private Long paymentTransactionId;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "payment_method_code", nullable = false, length = 30)
    private String paymentMethodCode;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "status", nullable = false, length = 30)
    private String status;  // SUCCESS, FAILED

    @Column(name = "provider_transaction_id", length = 100)
    private String providerTransactionId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        if (processedAt == null) processedAt = LocalDateTime.now();
    }

    public void markSuccess(String providerTransactionId) {
        this.status = "SUCCESS";
        this.providerTransactionId = providerTransactionId;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }
}